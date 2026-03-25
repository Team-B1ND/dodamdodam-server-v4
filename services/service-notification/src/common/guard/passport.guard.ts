import { CanActivate, ExecutionContext, HttpException, Injectable } from '@nestjs/common';
import { gunzipSync } from 'zlib';
import { BaseResponse } from '../response/base.response';

@Injectable()
export class PassportGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const raw = request.headers['x-user-passport'];
    if (!raw) {
      throw new HttpException(BaseResponse.error(401, '로그인이 필요해요.', 'AUTHENTICATION_REQUIRED'), 401);
    }

    try {
      const compressed = Buffer.from(raw, 'base64');
      const json = gunzipSync(compressed).toString('utf-8');
      const passport = JSON.parse(json);

      if (!passport.userId) {
        throw new HttpException(BaseResponse.error(401, '로그인이 필요해요.', 'AUTHENTICATION_REQUIRED'), 401);
      }

      if (passport.enabled === false) {
        throw new HttpException(BaseResponse.error(403, '사용자가 비활성 상태이거나 접근 권한이 없어요.', 'USER_DISABLED'), 403);
      }

      request.userPublicId = passport.userId;
      request.userRoles = passport.role ?? [];
      return true;
    } catch (e) {
      if (e instanceof HttpException) throw e;
      throw new HttpException(BaseResponse.error(401, '로그인이 필요해요.', 'AUTHENTICATION_REQUIRED'), 401);
    }
  }
}
