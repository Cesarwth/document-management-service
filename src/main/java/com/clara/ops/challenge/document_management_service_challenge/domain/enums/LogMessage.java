package com.clara.ops.challenge.document_management_service_challenge.domain.enums;

import org.slf4j.event.Level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogMessage {
  UPLOAD_REQUEST_RECEIVED(
      Level.INFO, "uploadDocument", "Received upload request for document: {} by user: {}"),
  UPLOAD_VALIDATION_FAILED_NULL_FILE(
      Level.ERROR, "validateFile", "Upload validation failed: File is null or empty"),
  UPLOAD_VALIDATION_FAILED_CONTENT_TYPE(
      Level.ERROR, "validateFile", "Upload validation failed: Invalid content type: {}"),
  UPLOAD_VALIDATION_FAILED_EXTENSION(
      Level.ERROR, "validateFile", "Upload validation failed: Invalid file extension: {}"),
  UPLOAD_VALIDATION_FAILED_SIZE(
      Level.ERROR, "validateFile", "Upload validation failed: File size {} exceeds maximum {}"),
  UPLOAD_VALIDATION_PASSED(
      Level.DEBUG, "validateFile", "File validation passed: {} (size: {} bytes, type: {})"),
  UPLOAD_SUCCESS(Level.INFO, "uploadDocument", "Document uploaded successfully: {}"),

  SEARCH_REQUEST_RECEIVED(
      Level.INFO, "searchDocuments", "Received search request - page: {}, size: {}, filters: {}"),
  SEARCH_VALIDATION_FAILED_PAGE(
      Level.ERROR, "validatePagination", "Pagination validation failed: Invalid page number: {}"),
  SEARCH_VALIDATION_FAILED_SIZE_MIN(
      Level.ERROR, "validatePagination", "Pagination validation failed: Invalid page size: {}"),
  SEARCH_VALIDATION_FAILED_SIZE_MAX(
      Level.ERROR,
      "validatePagination",
      "Pagination validation failed: Page size {} exceeds maximum {}"),
  SEARCH_VALIDATION_PASSED(
      Level.DEBUG, "validatePagination", "Pagination validation passed: page={}, size={}"),
  SEARCH_COMPLETED(
      Level.INFO, "searchDocuments", "Search completed - found {} documents out of {} total"),

  DOWNLOAD_REQUEST_RECEIVED(
      Level.INFO, "getDownloadUrl", "Received download request for document: {}"),
  DOWNLOAD_VALIDATION_FAILED_NULL_ID(
      Level.ERROR, "validateDocumentId", "Document ID validation failed: ID is null or empty"),
  DOWNLOAD_VALIDATION_FAILED_UUID(
      Level.ERROR, "validateDocumentId", "Document ID validation failed: Invalid UUID format: {}"),
  DOWNLOAD_VALIDATION_PASSED(
      Level.DEBUG, "validateDocumentId", "Document ID validation passed: {}"),
  DOWNLOAD_SUCCESS(
      Level.INFO, "getDownloadUrl", "Download URL generated successfully for document: {}"),

  SERVICE_UPLOAD_STARTED(Level.INFO, "uploadDocument", "Uploading document: {} for user: {}"),
  SERVICE_UPLOAD_SUCCESS(Level.INFO, "uploadDocument", "Document uploaded successfully: {}"),
  SERVICE_UPLOAD_ERROR(Level.ERROR, "uploadDocument", "Error uploading document"),

  SERVICE_SEARCH_STARTED(Level.INFO, "searchDocuments", "Searching documents with filters: {}"),

  SERVICE_DOWNLOAD_STARTED(
      Level.INFO, "getDownloadUrl", "Generating download URL for document: {}"),

  MINIO_UPLOAD_STARTED(Level.INFO, "uploadFile", "Uploading file to MinIO: {}"),
  MINIO_UPLOAD_SUCCESS(Level.INFO, "uploadFile", "File uploaded successfully to MinIO: {}"),
  MINIO_UPLOAD_ERROR(Level.ERROR, "uploadFile", "Error uploading file to MinIO: {}"),
  MINIO_URL_GENERATION_STARTED(
      Level.INFO, "generatePresignedUrl", "Generating presigned URL for: {}"),
  MINIO_URL_GENERATION_SUCCESS(
      Level.INFO, "generatePresignedUrl", "Presigned URL generated successfully for: {}"),
  MINIO_URL_GENERATION_ERROR(
      Level.ERROR, "generatePresignedUrl", "Error generating presigned URL for: {}"),

  MINIO_BUCKET_CREATING(Level.INFO, "minioClient", "Creating bucket: {}"),
  MINIO_BUCKET_CREATED(Level.INFO, "minioClient", "Bucket created successfully: {}"),
  MINIO_BUCKET_EXISTS(Level.INFO, "minioClient", "Bucket already exists: {}"),
  MINIO_BUCKET_ERROR(Level.ERROR, "minioClient", "Error checking/creating bucket"),

  EXCEPTION_DOCUMENT_NOT_FOUND(Level.ERROR, "handleDocumentNotFound", "Document not found: {}"),
  EXCEPTION_UPLOAD_ERROR(Level.ERROR, "handleDocumentUpload", "Document upload error: {}"),
  EXCEPTION_INVALID_DOCUMENT(Level.ERROR, "handleInvalidDocument", "Invalid document: {}"),
  EXCEPTION_VALIDATION_ERROR(Level.ERROR, "handleValidation", "Validation error: {}"),
  EXCEPTION_FILE_SIZE_EXCEEDED(Level.ERROR, "handleMaxUploadSize", "File size exceeds maximum: {}"),
  EXCEPTION_UNEXPECTED(Level.ERROR, "handleGeneric", "Unexpected error: {}");

  private final Level level;
  private final String method;
  private final String messageTemplate;

  public String format(Object... params) {
    String message = messageTemplate;
    for (Object param : params) {
      message = message.replaceFirst("\\{\\}", String.valueOf(param));
    }
    return message;
  }

  public String getMessage() {
    return messageTemplate;
  }

  public String getContext() {
    return method;
  }
}
