import { Body, Controller, Delete, HttpCode, Post, Req, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { PassportGuard } from '../../common/guard/passport.guard';
import { DeviceTokenRepository } from '../../domain/device-token/device-token.repository';
import { BaseResponse } from '../../common/response/base.response';
import { RegisterDeviceTokenDto, DeleteDeviceTokenDto } from './dto/device-token.dto';

@ApiTags('Device Token')
@Controller('device-tokens')
@UseGuards(PassportGuard)
@ApiBearerAuth()
export class DeviceTokenController {
  constructor(private readonly deviceTokenRepo: DeviceTokenRepository) {}

  @Post()
  @HttpCode(201)
  @ApiOperation({ summary: 'FCM 디바이스 토큰 등록' })
  async register(@Req() req: any, @Body() dto: RegisterDeviceTokenDto) {
    await this.deviceTokenRepo.upsert(req.userPublicId, dto.fcmToken, dto.platform);
    return BaseResponse.created('디바이스 토큰 등록에 성공했어요.');
  }

  @Delete()
  @ApiOperation({ summary: 'FCM 디바이스 토큰 삭제' })
  async remove(@Req() req: any, @Body() dto: DeleteDeviceTokenDto) {
    await this.deviceTokenRepo.deleteByUserAndToken(req.userPublicId, dto.fcmToken);
    return BaseResponse.ok('디바이스 토큰 삭제에 성공했어요.');
  }
}
