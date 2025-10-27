package com.clara.ops.challenge.document_management_service_challenge.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidationMessage {
  FILE_REQUIRED("File is required and cannot be empty"),
  INVALID_FILE_TYPE("Only PDF files are allowed. Received content type: %s"),
  INVALID_FILE_EXTENSION("File must have .pdf extension"),
  FILE_SIZE_EXCEEDED("File size exceeds maximum allowed size of %d MB"),
  PAGE_NUMBER_INVALID("Page number must be greater than or equal to 0"),
  PAGE_SIZE_TOO_SMALL("Page size must be greater than 0"),
  PAGE_SIZE_TOO_LARGE("Page size must not exceed %d items"),
  DOCUMENT_ID_REQUIRED("Document ID is required"),
  DOCUMENT_ID_INVALID_FORMAT("Invalid document ID format. Expected UUID format.");

  private final String message;

  public String format(Object... args) {
    return String.format(message, args);
  }
}
