import { Module, Global } from '@nestjs/common';
import { AppQueryClient } from './app-query.client';

@Global()
@Module({
  providers: [AppQueryClient],
  exports: [AppQueryClient],
})
export class GrpcModule {}
