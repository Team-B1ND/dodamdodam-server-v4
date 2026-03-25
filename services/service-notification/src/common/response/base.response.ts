import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class BaseResponse<T> {
  @ApiProperty({ example: 200 })
  status!: number;

  @ApiProperty({ example: '요청이 성공했어요.' })
  message!: string;

  @ApiPropertyOptional()
  data?: T;

  @ApiPropertyOptional()
  code?: string;

  static ok<T>(message: string, data?: T): BaseResponse<T> {
    const res = new BaseResponse<T>();
    res.status = 200;
    res.message = message;
    res.data = data;
    return res;
  }

  static created<T>(message: string, data?: T): BaseResponse<T> {
    const res = new BaseResponse<T>();
    res.status = 201;
    res.message = message;
    res.data = data;
    return res;
  }

  static error(status: number, message: string, code?: string): BaseResponse<null> {
    const res = new BaseResponse<null>();
    res.status = status;
    res.message = message;
    res.code = code;
    return res;
  }
}
