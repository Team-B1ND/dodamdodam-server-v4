import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, Index } from 'typeorm';

@Entity('device_tokens')
@Index('idx_user_token', ['userPublicId', 'fcmToken'], { unique: true })
@Index('idx_user', ['userPublicId'])
export class DeviceTokenEntity {
  @PrimaryGeneratedColumn({ type: 'bigint' })
  id!: number;

  @Column({ name: 'user_public_id', length: 64 })
  userPublicId!: string;

  @Column({ name: 'fcm_token', length: 512 })
  fcmToken!: string;

  @Column({ length: 20 })
  platform!: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt!: Date;
}
