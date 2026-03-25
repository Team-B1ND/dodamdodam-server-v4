import { CanActivate, ExecutionContext, HttpException, Injectable } from '@nestjs/common';
import { BaseResponse } from '../response/base.response';

@Injectable()
export class AdminGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const roles: string[] = request.userRoles ?? [];
    if (!roles.includes('ADMIN')) {
      throw new HttpException(BaseResponse.error(403, '접근 권한이 없어요.', 'ACCESS_DENIED'), 403);
    }
    return true;
  }
}
