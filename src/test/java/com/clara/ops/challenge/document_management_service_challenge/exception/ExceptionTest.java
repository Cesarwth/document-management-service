package com.clara.ops.challenge.document_management_service_challenge.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

  @Test
  void shouldCreateDocumentNotFoundException() {
    String message = "Document not found";
    DocumentNotFoundException exception = new DocumentNotFoundException(message);

    assertThat(exception).isInstanceOf(RuntimeException.class);
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  void shouldCreateDocumentUploadException() {
    String message = "Upload failed";
    DocumentUploadException exception = new DocumentUploadException(message);

    assertThat(exception).isInstanceOf(RuntimeException.class);
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  void shouldCreateDocumentUploadExceptionWithCause() {
    String message = "Upload failed";
    Throwable cause = new RuntimeException("Root cause");
    DocumentUploadException exception = new DocumentUploadException(message, cause);

    assertThat(exception).isInstanceOf(RuntimeException.class);
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  void shouldCreateInvalidDocumentException() {
    String message = "Invalid document";
    InvalidDocumentException exception = new InvalidDocumentException(message);

    assertThat(exception).isInstanceOf(RuntimeException.class);
    assertThat(exception.getMessage()).isEqualTo(message);
  }
}
