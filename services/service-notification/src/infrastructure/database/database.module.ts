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
        type: config.get('DATABASE_DRIVER', 'mysql') as any,
        host: config.get('DATABASE_HOST', 'localhost'),
        port: config.get<number>('DATABASE_PORT', 3306),
        database: 'dodam-notification',
        username: config.get('DATABASE_USER', 'root'),
        password: config.get('DATABASE_PASSWORD', ''),
        entities: [DeviceTokenEntity, NotificationLogEntity],
        synchronize: config.get('NODE_ENV') !== 'production',
        logging: config.get('NODE_ENV') !== 'production',
      }),
    }),
  ],
})
export class DatabaseModule {}
