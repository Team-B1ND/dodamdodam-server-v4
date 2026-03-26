import { Injectable, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import { join } from 'path';

export interface AppInfo {
  appPublicId: string;
  name: string;
  iconUrl: string;
  darkIconUrl: string;
  appUrl: string;
}

@Injectable()
export class AppQueryClient implements OnModuleInit {
  private client: any;

  constructor(private readonly config: ConfigService) {}

  onModuleInit() {
    const protoPath = join(__dirname, '../../..', 'proto/inapp/app_query.proto');
    const packageDef = protoLoader.loadSync(protoPath, {
      keepCase: false,
      longs: String,
      enums: String,
      defaults: true,
      oneofs: true,
    });
    const proto = grpc.loadPackageDefinition(packageDef) as any;
    const address = this.config.get('GRPC_INAPP_ADDRESS', 'localhost:9093');
    this.client = new proto.inapp.AppQueryService(address, grpc.credentials.createInsecure());
  }

  getApp(appPublicId: string): Promise<AppInfo> {
    return new Promise((resolve, reject) => {
      this.client.GetApp({ appPublicId }, (err: grpc.ServiceError | null, res: AppInfo) => {
        if (err) return reject(err);
        resolve(res);
      });
    });
  }
}
