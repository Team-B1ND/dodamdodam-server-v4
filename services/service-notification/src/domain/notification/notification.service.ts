import { Injectable, Logger } from '@nestjs/common';
import { FcmService, FcmPayload } from '../../infrastructure/firebase/fcm.service';
import { KafkaProducerService } from '../../infrastructure/kafka/kafka-producer.service';
import { DeviceTokenRepository } from '../device-token/device-token.repository';
import { NotificationRepository } from './notification.repository';
import { AppInfo } from '../../infrastructure/grpc/app-query.client';

export interface NotificationTarget {
  appInfo: AppInfo;
  title: string;
  body: string;
  targetUserPublicIds: string[];
  extraData: Record<string, string>;
}

export interface NotificationResult {
  successCount: number;
  failureCount: number;
}

const MAX_RETRY = 1;
const NOTIFICATION_FAILED_TOPIC = 'notification.failed';

@Injectable()
export class NotificationService {
  private readonly logger = new Logger(NotificationService.name);

  constructor(
    private readonly fcmService: FcmService,
    private readonly kafkaProducer: KafkaProducerService,
    private readonly deviceTokenRepo: DeviceTokenRepository,
    private readonly notificationRepo: NotificationRepository,
  ) {}

  async send(target: NotificationTarget): Promise<NotificationResult> {
    const payload = this.buildPayload(target);

    try {
      if (target.targetUserPublicIds.length === 0) {
        return await this.broadcastWithRetry(target, payload);
      }
      return await this.sendToUsersWithRetry(target, payload);
    } catch (err) {
      await this.publishFailedEvent(target, err);
      return { successCount: 0, failureCount: 1 };
    }
  }

  private buildPayload(target: NotificationTarget): FcmPayload {
    return {
      title: target.title,
      body: target.body,
      imageUrl: target.appInfo.iconUrl || undefined,
      data: {
        appPublicId: target.appInfo.appPublicId,
        appName: target.appInfo.name,
        ...(target.appInfo.iconUrl ? { iconUrl: target.appInfo.iconUrl } : {}),
        ...(target.appInfo.appUrl ? { appUrl: target.appInfo.appUrl } : {}),
        ...target.extraData,
      },
    };
  }

  private async broadcastWithRetry(target: NotificationTarget, payload: FcmPayload): Promise<NotificationResult> {
    let lastError: unknown;

    for (let attempt = 0; attempt <= MAX_RETRY; attempt++) {
      try {
        const sent = await this.fcmService.sendToTopic(`app-${target.appInfo.appPublicId}`, payload);
        return { successCount: sent ? 1 : 0, failureCount: sent ? 0 : 1 };
      } catch (err) {
        lastError = err;
        this.logger.warn(`Broadcast attempt ${attempt + 1} failed`, err);
      }
    }

    throw lastError;
  }

  private async sendToUsersWithRetry(target: NotificationTarget, payload: FcmPayload): Promise<NotificationResult> {
    const deviceTokens = await this.deviceTokenRepo.findByUserPublicIds(target.targetUserPublicIds);
    if (deviceTokens.length === 0) {
      return { successCount: 0, failureCount: 0 };
    }

    const tokens = deviceTokens.map(dt => dt.fcmToken);
    const tokenToUser = new Map(deviceTokens.map(dt => [dt.fcmToken, dt.userPublicId]));

    let totalSuccess = 0;
    let totalFailure = 0;
    const logs: Array<{ appPublicId: string; userPublicId: string; title: string; body: string; status: 'SENT' | 'FAILED'; errorMessage?: string }> = [];
    const staleTokens: string[] = [];

    const BATCH_SIZE = 500;
    for (let i = 0; i < tokens.length; i += BATCH_SIZE) {
      const batch = tokens.slice(i, i + BATCH_SIZE);
      const result = await this.sendBatchWithRetry(batch, payload);

      totalSuccess += result.successCount;
      totalFailure += batch.length - result.successCount;
      staleTokens.push(...result.failedTokens);

      for (const token of batch) {
        const failed = result.failedTokens.includes(token);
        logs.push({
          appPublicId: target.appInfo.appPublicId,
          userPublicId: tokenToUser.get(token) ?? '',
          title: target.title,
          body: target.body,
          status: failed ? 'FAILED' : 'SENT',
          errorMessage: failed ? 'token_not_registered' : undefined,
        });
      }
    }

    this.cleanupStaleTokens(staleTokens);
    this.saveLogs(logs);

    return { successCount: totalSuccess, failureCount: totalFailure };
  }

  private async sendBatchWithRetry(tokens: string[], payload: FcmPayload): Promise<{ successCount: number; failedTokens: string[] }> {
    let lastError: unknown;

    for (let attempt = 0; attempt <= MAX_RETRY; attempt++) {
      try {
        return await this.fcmService.sendToTokens(tokens, payload);
      } catch (err) {
        lastError = err;
        this.logger.warn(`sendToTokens attempt ${attempt + 1} failed`, err);
      }
    }

    throw lastError;
  }

  private async publishFailedEvent(target: NotificationTarget, err: unknown) {
    const errorMessage = err instanceof Error ? err.message : String(err);
    this.logger.error(`Notification failed after ${MAX_RETRY + 1} attempts: ${errorMessage}`);

    try {
      await this.kafkaProducer.send(NOTIFICATION_FAILED_TOPIC, {
        appPublicId: target.appInfo.appPublicId,
        appName: target.appInfo.name,
        title: target.title,
        body: target.body,
        targetUserPublicIds: target.targetUserPublicIds,
        errorMessage,
        retryCount: MAX_RETRY + 1,
        occurredAt: new Date().toISOString(),
      });
    } catch (kafkaErr) {
      this.logger.error('Failed to publish notification.failed event', kafkaErr);
    }
  }

  private cleanupStaleTokens(tokens: string[]) {
    if (tokens.length === 0) return;
    this.deviceTokenRepo.deleteByFcmTokens(tokens).catch(err => this.logger.error('Failed to cleanup stale tokens', err));
  }

  private saveLogs(logs: Array<{ appPublicId: string; userPublicId: string; title: string; body: string; status: 'SENT' | 'FAILED'; errorMessage?: string }>) {
    if (logs.length === 0) return;
    this.notificationRepo.insertBatch(logs).catch(err => this.logger.error('Failed to insert notification logs', err));
  }
}
