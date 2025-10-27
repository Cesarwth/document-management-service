package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentUploadException;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

  @Mock private MinioClient minioClient;

  @Mock private MinioProperties minioProperties;

  @Mock private ObjectWriteResponse objectWriteResponse;

  @InjectMocks private MinioService minioService;

  @BeforeEach
  void setUp() {
    lenient().when(minioProperties.getBucketName()).thenReturn("test-bucket");
    lenient().when(minioProperties.getPresignedUrlExpirySeconds()).thenReturn(3600);
  }

  @Test
  void shouldUploadFileSuccessfullyWhenInputIsValid() throws Exception {
    InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
    String objectPath = "user/document.pdf";
    String contentType = "application/pdf";
    long fileSize = 1024L;

    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(objectWriteResponse);

    minioService.uploadFile(inputStream, objectPath, contentType, fileSize);

    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
  }

  @Test
  void shouldThrowDocumentUploadExceptionWhenMinioFailsDuringUpload() throws Exception {
    InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
    String objectPath = "user/document.pdf";
    String contentType = "application/pdf";
    long fileSize = 1024L;

    when(minioClient.putObject(any(PutObjectArgs.class)))
        .thenThrow(new RuntimeException("MinIO error"));

    assertThatThrownBy(
            () -> minioService.uploadFile(inputStream, objectPath, contentType, fileSize))
        .isInstanceOf(DocumentUploadException.class)
        .hasMessageContaining("Failed to upload file to storage");
  }

  @Test
  void shouldReturnPresignedUrlWhenInputIsValid() throws Exception {
    String objectPath = "user/document.pdf";
    String expectedUrl = "http://localhost:9000/test-bucket/user/document.pdf?presigned=true";

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenReturn(expectedUrl);

    String actualUrl = minioService.generatePresignedUrl(objectPath);

    assertThat(actualUrl).isEqualTo(expectedUrl);
    verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

  @Test
  void shouldThrowDocumentUploadExceptionWhenMinioFailsGeneratingUrl() throws Exception {
    String objectPath = "user/document.pdf";

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenThrow(new RuntimeException("MinIO error"));

    assertThatThrownBy(() -> minioService.generatePresignedUrl(objectPath))
        .isInstanceOf(DocumentUploadException.class)
        .hasMessageContaining("Failed to generate download URL");
  }
}
