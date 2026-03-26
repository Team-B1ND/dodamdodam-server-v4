import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DeviceTokenModule } from '../device-token/device-token.module';
import { NotificationLogEntity } from './notification-log.entity';
import { NotificationRepository } from './notification.repository';
import { NotificationService } from './notification.service';
import { SendNotificationUseCase } from '../../application/send-notification.usecase';
import { NotificationController } from '../../presentation/http/notification.controller';
import { NotificationKafkaController } from '../../presentation/kafka/notification.kafka-controller';

@Module({
  imports: [TypeOrmModule.forFeature([NotificationLogEntity]), DeviceTokenModule],
  providers: [NotificationRepository, NotificationService, SendNotificationUseCase],
  controllers: [NotificationController, NotificationKafkaController],
})
export class NotificationModule {}
