import { IsString, IsIn } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class RegisterDeviceTokenDto {
  @ApiProperty({ description: 'FCM 디바이스 토큰' })
  @IsString()
  fcmToken!: string;

  @ApiProperty({ description: '플랫폼', enum: ['ANDROID', 'IOS', 'WEB'] })
  @IsIn(['ANDROID', 'IOS', 'WEB'])
  platform!: string;
}

export class DeleteDeviceTokenDto {
  @ApiProperty({ description: '삭제할 FCM 디바이스 토큰' })
  @IsString()
  fcmToken!: string;
}
