import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigService } from '@nestjs/config';
import { DeviceTokenEntity } from '../../domain/device-token/device-token.entity';
import { NotificationLogEntity } from '../../domain/notification/notification-log.entity';

@Module({
  imports: [
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        type: 'mysql',
        host: config.get('DB_HOST', 'localhost'),
        port: config.get<number>('DB_PORT', 3306),
        database: config.get('DB_NAME', 'dodam_notification'),
        username: config.get('DB_USER', 'root'),
        password: config.get('DB_PASSWORD', ''),
        entities: [DeviceTokenEntity, NotificationLogEntity],
        synchronize: config.get('NODE_ENV') !== 'production',
        logging: config.get('NODE_ENV') !== 'production',
      }),
    }),
  ],
})
export class DatabaseModule {}
