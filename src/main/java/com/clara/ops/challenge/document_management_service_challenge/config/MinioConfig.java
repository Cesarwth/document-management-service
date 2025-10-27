package com.clara.ops.challenge.document_management_service_challenge.config;

import com.clara.ops.challenge.document_management_service_challenge.domain.enums.LogMessage;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile("!test")
public class MinioConfig {

  private final MinioProperties minioProperties;

  @Bean
  public MinioClient minioClient() {
    String endpoint =
        Optional.ofNullable(minioProperties.getEndpoint())
            .filter(e -> !e.isBlank())
            .orElseThrow(() -> new IllegalStateException("MinIO endpoint is not configured"));

    String accessKey =
        Optional.ofNullable(minioProperties.getAccessKey())
            .filter(k -> !k.isBlank())
            .orElseThrow(() -> new IllegalStateException("MinIO access key is not configured"));

    String secretKey =
        Optional.ofNullable(minioProperties.getSecretKey())
            .filter(k -> !k.isBlank())
            .orElseThrow(() -> new IllegalStateException("MinIO secret key is not configured"));

    String bucketName =
        Optional.ofNullable(minioProperties.getBucketName())
            .filter(b -> !b.isBlank())
            .orElseThrow(() -> new IllegalStateException("MinIO bucket name is not configured"));

    MinioClient minioClient =
        MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();

    try {
      boolean bucketExists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!bucketExists) {
        log.info(LogMessage.MINIO_BUCKET_CREATING.getMessage(), bucketName);
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        log.info(LogMessage.MINIO_BUCKET_CREATED.getMessage(), bucketName);
      } else {
        log.info(LogMessage.MINIO_BUCKET_EXISTS.getMessage(), bucketName);
      }
    } catch (Exception e) {
      log.error(LogMessage.MINIO_BUCKET_ERROR.getMessage(), e);
      throw new RuntimeException("Failed to initialize MinIO bucket", e);
    }

    return minioClient;
  }
}
