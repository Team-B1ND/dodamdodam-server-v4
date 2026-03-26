import { Body, Controller, Get, Param, ParseIntPipe, Patch, Post, Query, Req, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { SendNotificationUseCase } from '../../application/send-notification.usecase';
import { PassportGuard } from '../../common/guard/passport.guard';
import { AdminGuard } from '../../common/guard/admin.guard';
import { NotificationRepository } from '../../domain/notification/notification.repository';
import { BaseResponse } from '../../common/response/base.response';
import { PageResponse } from '../../common/response/page.response';
import { SendNotificationDto } from './dto/send-notification.dto';

@ApiTags('Notification')
@Controller()
export class NotificationController {
  constructor(
    private readonly useCase: SendNotificationUseCase,
    private readonly notificationRepo: NotificationRepository,
  ) {}

  @Get('health')
  @ApiOperation({ summary: '헬스체크' })
  health() {
    return BaseResponse.ok('서비스가 정상 동작 중이에요.');
  }

  @Post('send')
  @ApiOperation({ summary: '알림 발송' })
  async send(@Body() dto: SendNotificationDto) {
    const result = await this.useCase.execute({
      appPublicId: dto.appPublicId,
      appName: dto.appName,
      title: dto.title,
      body: dto.body,
      targetUserPublicIds: dto.targetUserPublicIds ?? [],
      data: dto.data ?? {},
    });
    return BaseResponse.ok('알림 발송에 성공했어요.', result);
  }

  @Get('my')
  @UseGuards(PassportGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: '내 알림 조회 (최근 7일)' })
  @ApiQuery({ name: 'limit', required: false, example: 20 })
  @ApiQuery({ name: 'offset', required: false, example: 0 })
  async getMyNotifications(@Req() req: any, @Query('limit') limit = 20, @Query('offset') offset = 0) {
    const numLimit = Number(limit);
    const logs = await this.notificationRepo.findByUserRecent(req.userPublicId, numLimit + 1, Number(offset));
    return BaseResponse.ok('알림 조회에 성공했어요.', PageResponse.of(logs, numLimit));
  }

  @Patch(':id/read')
  @UseGuards(PassportGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: '알림 읽음 처리' })
  async markAsRead(@Req() req: any, @Param('id', ParseIntPipe) id: number) {
    await this.notificationRepo.markAsRead(id, req.userPublicId);
    return BaseResponse.ok('알림을 읽음 처리했어요.');
  }

  @Patch('read-all')
  @UseGuards(PassportGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: '전체 알림 읽음 처리' })
  async markAllAsRead(@Req() req: any) {
    await this.notificationRepo.markAllAsRead(req.userPublicId);
    return BaseResponse.ok('모든 알림을 읽음 처리했어요.');
  }

  @Get('all')
  @UseGuards(PassportGuard, AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: '전체 알림 조회 (어드민 전용)' })
  @ApiQuery({ name: 'limit', required: false, example: 50 })
  @ApiQuery({ name: 'offset', required: false, example: 0 })
  async getAllNotifications(@Query('limit') limit = 50, @Query('offset') offset = 0) {
    const numLimit = Number(limit);
    const logs = await this.notificationRepo.findAll(numLimit + 1, Number(offset));
    return BaseResponse.ok('전체 알림 조회에 성공했어요.', PageResponse.of(logs, numLimit));
  }
}
