import { Injectable } from '@nestjs/common';
import { AppQueryClient, AppInfo } from '../infrastructure/grpc/app-query.client';
import { NotificationService, NotificationResult } from '../domain/notification/notification.service';

export interface SendNotificationCommand {
  appPublicId?: string;
  appName?: string;
  title: string;
  body: string;
  targetUserPublicIds: string[];
  data: Record<string, string>;
}

@Injectable()
export class SendNotificationUseCase {
  constructor(
    private readonly appQueryClient: AppQueryClient,
    private readonly notificationService: NotificationService,
  ) {}

  async execute(command: SendNotificationCommand): Promise<NotificationResult> {
    const appInfo = command.appPublicId
      ? await this.appQueryClient.getApp(command.appPublicId)
      : this.buildInternalAppInfo(command.appName!);

    return this.notificationService.send({
      appInfo,
      title: command.title,
      body: command.body,
      targetUserPublicIds: command.targetUserPublicIds,
      extraData: command.data,
    });
  }

  private buildInternalAppInfo(appName: string): AppInfo {
    return {
      appPublicId: appName,
      name: appName,
      iconUrl: '',
      darkIconUrl: '',
      appUrl: '',
    };
  }
}
