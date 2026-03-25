import { Controller, Logger } from '@nestjs/common';
import { EventPattern, Payload } from '@nestjs/microservices';
import { SendNotificationUseCase } from '../../application/send-notification.usecase';

interface NotificationSendEvent {
  appPublicId?: string;
  appName?: string;
  title: string;
  body: string;
  targetUserPublicIds: string[];
  data: Record<string, string>;
}

@Controller()
export class NotificationKafkaController {
  private readonly logger = new Logger(NotificationKafkaController.name);

  constructor(private readonly useCase: SendNotificationUseCase) {}

  @EventPattern('notification.send')
  async handleNotificationSend(@Payload() event: NotificationSendEvent) {
    try {
      await this.useCase.execute({
        appPublicId: event.appPublicId,
        appName: event.appName,
        title: event.title,
        body: event.body,
        targetUserPublicIds: event.targetUserPublicIds ?? [],
        data: event.data ?? {},
      });
    } catch (err) {
      this.logger.error('Failed to process notification event', err);
    }
  }
}
