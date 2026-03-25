import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, Index } from 'typeorm';

@Entity('notification_logs')
@Index('idx_user_created', ['userPublicId', 'createdAt'])
export class NotificationLogEntity {
  @PrimaryGeneratedColumn({ type: 'bigint' })
  id!: number;

  @Column({ name: 'app_public_id', length: 64 })
  appPublicId!: string;

  @Column({ name: 'user_public_id', length: 64 })
  userPublicId!: string;

  @Column({ length: 256 })
  title!: string;

  @Column({ type: 'text' })
  body!: string;

  @Column({ length: 20 })
  status!: string;

  @Column({ name: 'is_read', default: false })
  isRead!: boolean;

  @Column({ name: 'error_message', type: 'text', nullable: true })
  errorMessage!: string | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;
}
