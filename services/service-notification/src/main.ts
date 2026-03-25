import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { AppModule } from './app.module';
import { GlobalExceptionFilter } from './common/filter/global-exception.filter';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.useGlobalPipes(new ValidationPipe({ transform: true, whitelist: true }));
  app.useGlobalFilters(new GlobalExceptionFilter());

  const swaggerConfig = new DocumentBuilder()
    .setTitle('Notification Service')
    .setDescription('도담도담 알림 서비스 API')
    .setVersion('1.0')
    .addServer('/notification')
    .addBearerAuth()
    .build();
  const document = SwaggerModule.createDocument(app, swaggerConfig);
  SwaggerModule.setup('swagger-ui', app, document, {
    swaggerOptions: { persistAuthorization: true },
  });

  const kafkaBrokers = process.env.KAFKA_BOOTSTRAP_SERVERS ?? 'localhost:5001';
  const kafkaGroupId = process.env.KAFKA_CONSUMER_GROUP_ID ?? 'service-notification';

  app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.KAFKA,
    options: {
      client: {
        brokers: kafkaBrokers.split(','),
        retry: { retries: 5 },
      },
      consumer: {
        groupId: kafkaGroupId,
        allowAutoTopicCreation: true,
      },
    },
  });

  await app.startAllMicroservices();

  const port = process.env.PORT ?? 8089;
  await app.listen(port, '0.0.0.0');
}

bootstrap();
