import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { GrpcModule } from './infrastructure/grpc/grpc.module';
import { DatabaseModule } from './infrastructure/database/database.module';
import { FirebaseModule } from './infrastructure/firebase/firebase.module';
import { KafkaProducerModule } from './infrastructure/kafka/kafka-producer.module';
import { NotificationModule } from './domain/notification/notification.module';
import { DeviceTokenModule } from './domain/device-token/device-token.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    DatabaseModule,
    GrpcModule,
    FirebaseModule,
    KafkaProducerModule,
    DeviceTokenModule,
    NotificationModule,
  ],
})
export class AppModule {}
