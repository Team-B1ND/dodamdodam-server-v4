import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import admin from 'firebase-admin';

export interface FcmPayload {
  title: string;
  body: string;
  imageUrl?: string;
  data?: Record<string, string>;
}

@Injectable()
export class FcmService implements OnModuleInit {
  private readonly logger = new Logger(FcmService.name);
  private initialized = false;

  constructor(private readonly config: ConfigService) {}

  onModuleInit() {
    const raw = this.config.get<string>('FIREBASE_SERVICE_ACCOUNT', '');
    if (!raw) {
      this.logger.warn('FIREBASE_SERVICE_ACCOUNT not set, FCM disabled');
      return;
    }
    const credential = raw.startsWith('{')
      ? JSON.parse(raw)
      : JSON.parse(Buffer.from(raw, 'base64').toString('utf-8'));

    admin.initializeApp({ credential: admin.credential.cert(credential) });
    this.initialized = true;
    this.logger.log('Firebase initialized');
  }

  get isEnabled(): boolean {
    return this.initialized;
  }

  async sendToTokens(tokens: string[], payload: FcmPayload): Promise<{ successCount: number; failedTokens: string[] }> {
    if (!this.initialized || tokens.length === 0) {
      return { successCount: 0, failedTokens: [] };
    }

    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      notification: {
        title: payload.title,
        body: payload.body,
        ...(payload.imageUrl ? { imageUrl: payload.imageUrl } : {}),
      },
      data: payload.data,
    });

    const failedTokens: string[] = [];
    response.responses.forEach((resp, idx) => {
      if (!resp.success) {
        const code = resp.error?.code;
        if (code === 'messaging/registration-token-not-registered' || code === 'messaging/invalid-registration-token') {
          failedTokens.push(tokens[idx]);
        }
      }
    });

    return { successCount: response.successCount, failedTokens };
  }

  async sendToTopic(topic: string, payload: FcmPayload): Promise<boolean> {
    if (!this.initialized) return false;

    await admin.messaging().send({
      topic,
      notification: {
        title: payload.title,
        body: payload.body,
        ...(payload.imageUrl ? { imageUrl: payload.imageUrl } : {}),
      },
      data: payload.data,
    });

    return true;
  }
}
