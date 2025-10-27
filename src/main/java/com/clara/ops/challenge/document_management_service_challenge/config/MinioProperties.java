package com.clara.ops.challenge.document_management_service_challenge.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
@Validated
public class MinioProperties {

  @NotBlank private String endpoint;

  @NotBlank private String accessKey;

  @NotBlank private String secretKey;

  @NotBlank private String bucketName;

  private Integer presignedUrlExpirySeconds = 3600;
}
