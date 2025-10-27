package com.clara.ops.challenge.document_management_service_challenge.validation;

import com.clara.ops.challenge.document_management_service_challenge.config.DocumentManagementProperties;
import com.clara.ops.challenge.document_management_service_challenge.domain.enums.FileType;
import com.clara.ops.challenge.document_management_service_challenge.domain.enums.LogMessage;
import com.clara.ops.challenge.document_management_service_challenge.domain.enums.ValidationMessage;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Validator for document-related operations. Centralizes validation logic to keep controllers and
 * services focused on their primary responsibilities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentValidator {

  private static final String UUID_REGEX =
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  private final DocumentManagementProperties properties;

  /**
   * Validates the uploaded file to ensure it meets requirements.
   *
   * @param file The file to validate
   * @throws InvalidDocumentException if validation fails
   */
  public void validateFile(MultipartFile file) {
    MultipartFile validFile =
        Optional.ofNullable(file)
            .filter(f -> !f.isEmpty())
            .orElseThrow(
                () -> {
                  log.error(LogMessage.UPLOAD_VALIDATION_FAILED_NULL_FILE.getMessage());
                  return new InvalidDocumentException(ValidationMessage.FILE_REQUIRED.getMessage());
                });

    String contentType =
        Optional.ofNullable(validFile.getContentType())
            .filter(FileType.PDF::matches)
            .orElseThrow(
                () -> {
                  log.error(
                      LogMessage.UPLOAD_VALIDATION_FAILED_CONTENT_TYPE.getMessage(),
                      validFile.getContentType());
                  return new InvalidDocumentException(
                      ValidationMessage.INVALID_FILE_TYPE.format(validFile.getContentType()));
                });

    String originalFilename =
        Optional.ofNullable(validFile.getOriginalFilename())
            .filter(FileType.PDF::hasExtension)
            .orElseThrow(
                () -> {
                  log.error(
                      LogMessage.UPLOAD_VALIDATION_FAILED_EXTENSION.getMessage(),
                      validFile.getOriginalFilename());
                  return new InvalidDocumentException(
                      ValidationMessage.INVALID_FILE_EXTENSION.getMessage());
                });

    long maxFileSizeBytes = properties.getUpload().getMaxFileSizeBytes();
    if (validFile.getSize() > maxFileSizeBytes) {
      log.error(
          LogMessage.UPLOAD_VALIDATION_FAILED_SIZE.getMessage(),
          validFile.getSize(),
          maxFileSizeBytes);
      throw new InvalidDocumentException(
          ValidationMessage.FILE_SIZE_EXCEEDED.format(properties.getUpload().getMaxFileSizeMb()));
    }

    log.debug(
        LogMessage.UPLOAD_VALIDATION_PASSED.getMessage(),
        originalFilename,
        validFile.getSize(),
        contentType);
  }

  /**
   * Validates pagination parameters.
   *
   * @param page Page number
   * @param size Page size
   * @throws InvalidDocumentException if parameters are invalid
   */
  public void validatePaginationParams(int page, int size) {
    if (page < 0) {
      log.error(LogMessage.SEARCH_VALIDATION_FAILED_PAGE.getMessage(), page);
      throw new InvalidDocumentException(ValidationMessage.PAGE_NUMBER_INVALID.getMessage());
    }

    if (size < 1) {
      log.error(LogMessage.SEARCH_VALIDATION_FAILED_SIZE_MIN.getMessage(), size);
      throw new InvalidDocumentException(ValidationMessage.PAGE_SIZE_TOO_SMALL.getMessage());
    }

    int maxSize = properties.getPagination().getMaxSize();
    if (size > maxSize) {
      log.error(LogMessage.SEARCH_VALIDATION_FAILED_SIZE_MAX.getMessage(), size, maxSize);
      throw new InvalidDocumentException(ValidationMessage.PAGE_SIZE_TOO_LARGE.format(maxSize));
    }

    log.debug(LogMessage.SEARCH_VALIDATION_PASSED.getMessage(), page, size);
  }

  /**
   * Validates document ID format (UUID).
   *
   * @param documentId The document ID to validate
   * @throws InvalidDocumentException if format is invalid
   */
  public void validateDocumentId(String documentId) {
    String validId =
        Optional.ofNullable(documentId)
            .filter(id -> !id.isBlank())
            .orElseThrow(
                () -> {
                  log.error(LogMessage.DOWNLOAD_VALIDATION_FAILED_NULL_ID.getMessage());
                  return new InvalidDocumentException(
                      ValidationMessage.DOCUMENT_ID_REQUIRED.getMessage());
                });

    Optional.of(validId)
        .filter(id -> id.matches(UUID_REGEX))
        .orElseThrow(
            () -> {
              log.error(LogMessage.DOWNLOAD_VALIDATION_FAILED_UUID.getMessage(), validId);
              return new InvalidDocumentException(
                  ValidationMessage.DOCUMENT_ID_INVALID_FORMAT.getMessage());
            });

    log.debug(LogMessage.DOWNLOAD_VALIDATION_PASSED.getMessage(), validId);
  }
}
