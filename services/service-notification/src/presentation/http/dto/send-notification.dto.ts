import { IsString, IsOptional, IsArray, ValidateIf } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class SendNotificationDto {
  @ApiPropertyOptional({ description: '외부 서비스 앱 publicId (appName과 택1)' })
  @IsOptional()
  @IsString()
  appPublicId?: string;

  @ApiPropertyOptional({ description: '내부 서비스 이름 (appPublicId와 택1)', example: '야간자습' })
  @ValidateIf(o => !o.appPublicId)
  @IsString()
  appName?: string;

  @ApiProperty({ description: '알림 제목', example: '새로운 알림' })
  @IsString()
  title!: string;

  @ApiProperty({ description: '알림 본문', example: '알림 내용입니다.' })
  @IsString()
  body!: string;

  @ApiPropertyOptional({ description: '대상 유저 publicId 목록 (비어있으면 브로드캐스트)', type: [String] })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  targetUserPublicIds?: string[];

  @ApiPropertyOptional({ description: '추가 데이터' })
  @IsOptional()
  data?: Record<string, string>;
}
