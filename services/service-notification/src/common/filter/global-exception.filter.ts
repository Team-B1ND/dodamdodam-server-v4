import { ExceptionFilter, Catch, ArgumentsHost, HttpException, Logger } from '@nestjs/common';
import { BaseResponse } from '../response/base.response';

@Catch()
export class GlobalExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(GlobalExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const res = ctx.getResponse();

    if (exception instanceof HttpException) {
      const status = exception.getStatus();
      const body = exception.getResponse();

      if (typeof body === 'object' && body !== null && 'status' in body && 'message' in body) {
        res.status(status).json(body);
        return;
      }

      const message = typeof body === 'string'
        ? body
        : (body as any).message ?? exception.message;

      res.status(status).json(BaseResponse.error(status, Array.isArray(message) ? message[0] : message));
      return;
    }

    this.logger.error('Unhandled exception', exception);
    res.status(500).json(BaseResponse.error(500, '서버 내부 오류가 발생했어요.', 'INTERNAL_SERVER_ERROR'));
  }
}
