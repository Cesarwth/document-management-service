package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.domain.enums.LogMessage;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentUploadException;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;

  /**
   * Uploads a file to MinIO using streaming to minimize memory usage. The file is uploaded in
   * chunks, never loading the entire file into memory.
   *
   * @param inputStream The input stream of the file
   * @param objectPath The path where the file will be stored in MinIO
   * @param contentType The content type of the file
   * @param fileSize The size of the file in bytes
   */
  public void uploadFile(
      InputStream inputStream, String objectPath, String contentType, long fileSize) {
    Optional.ofNullable(inputStream)
        .orElseThrow(() -> new InvalidDocumentException("Input stream cannot be null"));
    String validObjectPath =
        Optional.ofNullable(objectPath)
            .filter(path -> !path.isBlank())
            .orElseThrow(() -> new InvalidDocumentException("Object path cannot be empty"));
    String validContentType =
        Optional.ofNullable(contentType)
            .filter(ct -> !ct.isBlank())
            .orElseThrow(() -> new InvalidDocumentException("Content type cannot be empty"));

    try {
      log.info(LogMessage.MINIO_UPLOAD_STARTED.getMessage(), validObjectPath);

      String bucketName =
          Optional.ofNullable(minioProperties.getBucketName())
              .filter(bn -> !bn.isBlank())
              .orElseThrow(() -> new InvalidDocumentException("Bucket name is not configured"));

      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(validObjectPath).stream(
                  inputStream, fileSize, -1) // -1 means use default part size (5MB chunks)
              .contentType(validContentType)
              .build());

      log.info(LogMessage.MINIO_UPLOAD_SUCCESS.getMessage(), validObjectPath);
    } catch (DocumentUploadException | InvalidDocumentException e) {
      throw e;
    } catch (Exception e) {
      log.error(LogMessage.MINIO_UPLOAD_ERROR.getMessage(), validObjectPath, e);
      throw new DocumentUploadException("Failed to upload file to storage", e);
    }
  }

  /**
   * Generates a presigned URL for downloading a file from MinIO. The URL expires after the
   * configured time.
   *
   * @param objectPath The path of the file in MinIO
   * @return The presigned URL
   */
  public String generatePresignedUrl(String objectPath) {
    String validObjectPath =
        Optional.ofNullable(objectPath)
            .filter(path -> !path.isBlank())
            .orElseThrow(() -> new InvalidDocumentException("Object path cannot be empty"));

    try {
      log.info(LogMessage.MINIO_URL_GENERATION_STARTED.getMessage(), validObjectPath);

      String bucketName =
          Optional.ofNullable(minioProperties.getBucketName())
              .filter(bn -> !bn.isBlank())
              .orElseThrow(() -> new InvalidDocumentException("Bucket name is not configured"));

      Integer expirySeconds =
          Optional.ofNullable(minioProperties.getPresignedUrlExpirySeconds())
              .orElseThrow(
                  () -> new InvalidDocumentException("Presigned URL expiry is not configured"));

      String url =
          minioClient.getPresignedObjectUrl(
              GetPresignedObjectUrlArgs.builder()
                  .method(Method.GET)
                  .bucket(bucketName)
                  .object(validObjectPath)
                  .expiry(expirySeconds, TimeUnit.SECONDS)
                  .build());

      return Optional.ofNullable(url)
          .filter(u -> !u.isBlank())
          .map(
              u -> {
                log.info(LogMessage.MINIO_URL_GENERATION_SUCCESS.getMessage(), validObjectPath);
                return u;
              })
          .orElseThrow(() -> new DocumentUploadException("Generated URL is empty"));
    } catch (DocumentUploadException | InvalidDocumentException e) {
      throw e;
    } catch (Exception e) {
      log.error(LogMessage.MINIO_URL_GENERATION_ERROR.getMessage(), validObjectPath, e);
      throw new DocumentUploadException("Failed to generate download URL", e);
    }
  }
}
