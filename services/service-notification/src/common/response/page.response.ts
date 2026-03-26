import { ApiProperty } from '@nestjs/swagger';

export class PageResponse<T> {
  @ApiProperty()
  content!: T[];

  @ApiProperty({ example: false })
  hasNext!: boolean;

  static of<T>(content: T[], limit: number): PageResponse<T> {
    const res = new PageResponse<T>();
    res.hasNext = content.length > limit;
    res.content = res.hasNext ? content.slice(0, limit) : content;
    return res;
  }
}
