package com.clara.ops.challenge.document_management_service_challenge.exception;

public class DocumentUploadException extends RuntimeException {
  public DocumentUploadException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocumentUploadException(String message) {
    super(message);
  }
}
