package com.clara.ops.challenge.document_management_service_challenge.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MinioConfigTest {

  private MinioConfig minioConfig;
  private MinioProperties minioProperties;

  @BeforeEach
  void setUp() {
    minioProperties = new MinioProperties();
    minioProperties.setEndpoint("http://localhost:9000");
    minioProperties.setAccessKey("minioadmin");
    minioProperties.setSecretKey("minioadmin");
    minioProperties.setBucketName("documents");
  }

  @Test
  void shouldThrowExceptionWhenEndpointIsNull() {
    minioProperties.setEndpoint(null);
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO endpoint");
  }

  @Test
  void shouldThrowExceptionWhenEndpointIsBlank() {
    minioProperties.setEndpoint("   ");
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO endpoint");
  }

  @Test
  void shouldThrowExceptionWhenAccessKeyIsNull() {
    minioProperties.setAccessKey(null);
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO access key");
  }

  @Test
  void shouldThrowExceptionWhenAccessKeyIsBlank() {
    minioProperties.setAccessKey("");
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO access key");
  }

  @Test
  void shouldThrowExceptionWhenSecretKeyIsNull() {
    minioProperties.setSecretKey(null);
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO secret key");
  }

  @Test
  void shouldThrowExceptionWhenSecretKeyIsBlank() {
    minioProperties.setSecretKey("  ");
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO secret key");
  }

  @Test
  void shouldThrowExceptionWhenBucketNameIsNull() {
    minioProperties.setBucketName(null);
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO bucket name");
  }

  @Test
  void shouldThrowExceptionWhenBucketNameIsEmpty() {
    minioProperties.setBucketName("");
    minioConfig = new MinioConfig(minioProperties);

    assertThatThrownBy(() -> minioConfig.minioClient())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MinIO bucket name");
  }

  @Test
  void shouldVerifyMinioPropertiesAccessors() {
    assertThat(minioProperties.getEndpoint()).isEqualTo("http://localhost:9000");
    assertThat(minioProperties.getAccessKey()).isEqualTo("minioadmin");
    assertThat(minioProperties.getSecretKey()).isEqualTo("minioadmin");
    assertThat(minioProperties.getBucketName()).isEqualTo("documents");
  }
}
