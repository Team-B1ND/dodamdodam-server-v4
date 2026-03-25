import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MoreThanOrEqual, Repository } from 'typeorm';
import { NotificationLogEntity } from './notification-log.entity';

@Injectable()
export class NotificationRepository {
  constructor(
    @InjectRepository(NotificationLogEntity)
    private readonly repo: Repository<NotificationLogEntity>,
  ) {}

  async insertBatch(logs: Partial<NotificationLogEntity>[]): Promise<void> {
    if (logs.length === 0) return;
    await this.repo.insert(logs);
  }

  findByUserRecent(userPublicId: string, limit: number, offset: number): Promise<NotificationLogEntity[]> {
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    return this.repo.find({
      where: { userPublicId, createdAt: MoreThanOrEqual(sevenDaysAgo) },
      order: { createdAt: 'DESC' },
      take: limit,
      skip: offset,
    });
  }

  findAll(limit: number, offset: number): Promise<NotificationLogEntity[]> {
    return this.repo.find({
      order: { createdAt: 'DESC' },
      take: limit,
      skip: offset,
    });
  }

  async markAsRead(id: number, userPublicId: string): Promise<boolean> {
    const result = await this.repo.update({ id, userPublicId }, { isRead: true });
    return (result.affected ?? 0) > 0;
  }

  async markAllAsRead(userPublicId: string): Promise<void> {
    await this.repo.update({ userPublicId, isRead: false }, { isRead: true });
  }
}
