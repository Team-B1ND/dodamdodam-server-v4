import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { DeviceTokenEntity } from './device-token.entity';

@Injectable()
export class DeviceTokenRepository {
  constructor(
    @InjectRepository(DeviceTokenEntity)
    private readonly repo: Repository<DeviceTokenEntity>,
  ) {}

  findByUserPublicIds(userPublicIds: string[]): Promise<DeviceTokenEntity[]> {
    if (userPublicIds.length === 0) return Promise.resolve([]);
    return this.repo.find({ where: { userPublicId: In(userPublicIds) } });
  }

  async upsert(userPublicId: string, fcmToken: string, platform: string): Promise<void> {
    await this.repo.upsert({ userPublicId, fcmToken, platform }, ['userPublicId', 'fcmToken']);
  }

  async deleteByUserAndToken(userPublicId: string, fcmToken: string): Promise<void> {
    await this.repo.delete({ userPublicId, fcmToken });
  }

  async deleteByFcmTokens(fcmTokens: string[]): Promise<void> {
    if (fcmTokens.length === 0) return;
    await this.repo.delete({ fcmToken: In(fcmTokens) });
  }
}
