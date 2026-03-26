import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DeviceTokenEntity } from './device-token.entity';
import { DeviceTokenRepository } from './device-token.repository';
import { DeviceTokenController } from '../../presentation/http/device-token.controller';

@Module({
  imports: [TypeOrmModule.forFeature([DeviceTokenEntity])],
  providers: [DeviceTokenRepository],
  controllers: [DeviceTokenController],
  exports: [DeviceTokenRepository],
})
export class DeviceTokenModule {}
