package com.clara.ops.challenge.document_management_service_challenge.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.clara.ops.challenge.document_management_service_challenge.config.DocumentManagementProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;

@ExtendWith(MockitoExtension.class)
class DocumentValidatorTest {

  private DocumentValidator documentValidator;
  private DocumentManagementProperties properties;

  @BeforeEach
  void setUp() {
    properties = new DocumentManagementProperties();
    DocumentManagementProperties.Upload upload = new DocumentManagementProperties.Upload();
    upload.setMaxFileSizeMb(500);
    properties.setUpload(upload);

    DocumentManagementProperties.Pagination pagination =
        new DocumentManagementProperties.Pagination();
    pagination.setDefaultSize(20);
    pagination.setMaxSize(100);
    properties.setPagination(pagination);

    documentValidator = new DocumentValidator(properties);
  }

  // ==================== validateFile Tests ====================

  @Test
  void shouldPassValidationForValidPdfFile() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());

    assertThatCode(() -> documentValidator.validateFile(file)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrowExceptionWhenFileIsNull() {
    assertThatThrownBy(() -> documentValidator.validateFile(null))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File is required");
  }

  @Test
  void shouldThrowExceptionWhenFileIsEmpty() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);

    assertThatThrownBy(() -> documentValidator.validateFile(file))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File is required");
  }

  @Test
  void shouldThrowExceptionWhenContentTypeIsNull() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", null, "test content".getBytes());

    assertThatThrownBy(() -> documentValidator.validateFile(file))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Only PDF files are allowed");
  }

  @Test
  void shouldThrowExceptionWhenContentTypeIsNotPdf() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

    assertThatThrownBy(() -> documentValidator.validateFile(file))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Only PDF files are allowed");
  }

  @Test
  void shouldThrowExceptionWhenFilenameIsNull() {
    MockMultipartFile file =
        new MockMultipartFile("file", null, "application/pdf", "test content".getBytes());

    assertThatThrownBy(() -> documentValidator.validateFile(file))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File must have .pdf extension");
  }

  @Test
  void shouldThrowExceptionWhenFileExtensionIsNotPdf() {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "application/pdf", "test content".getBytes());

    assertThatThrownBy(() -> documentValidator.validateFile(file))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File must have .pdf extension");
  }

  @Test
  void shouldThrowExceptionWhenFileSizeExceedsLimit() {
    byte[] largeContent = new byte[525 * 1024 * 1024]; // 525 MB
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", largeContent);

    assertThatThrownBy(() -> documentValidator.validateFile(file))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File size exceeds maximum");
  }

  @Test
  void shouldPassValidationForFileAtSizeLimit() {
    byte[] exactContent = new byte[500 * 1024 * 1024]; // Exactly 500 MB
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", exactContent);

    assertThatCode(() -> documentValidator.validateFile(file)).doesNotThrowAnyException();
  }

  @Test
  void shouldPassValidationForValidPaginationParams() {
    assertThatCode(() -> documentValidator.validatePaginationParams(0, 20))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrowExceptionWhenPageIsNegative() {
    assertThatThrownBy(() -> documentValidator.validatePaginationParams(-1, 20))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Page number must be greater than or equal to 0");
  }

  @Test
  void shouldThrowExceptionWhenSizeIsZero() {
    assertThatThrownBy(() -> documentValidator.validatePaginationParams(0, 0))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Page size must be greater than 0");
  }

  @Test
  void shouldThrowExceptionWhenSizeIsNegative() {
    assertThatThrownBy(() -> documentValidator.validatePaginationParams(0, -1))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Page size must be greater than 0");
  }

  @Test
  void shouldThrowExceptionWhenSizeExceedsMaximum() {
    assertThatThrownBy(() -> documentValidator.validatePaginationParams(0, 101))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Page size must not exceed");
  }

  @Test
  void shouldPassValidationForSizeAtMaximum() {
    assertThatCode(() -> documentValidator.validatePaginationParams(0, 100))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldPassValidationForMinimumSize() {
    assertThatCode(() -> documentValidator.validatePaginationParams(0, 1))
        .doesNotThrowAnyException();
  }

  // ==================== validateDocumentId Tests ====================

  @Test
  void shouldPassValidationForValidUUID() {
    String validUuid = "123e4567-e89b-12d3-a456-426614174000";
    assertThatCode(() -> documentValidator.validateDocumentId(validUuid))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrowExceptionWhenDocumentIdIsNull() {
    assertThatThrownBy(() -> documentValidator.validateDocumentId(null))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Document ID is required");
  }

  @Test
  void shouldThrowExceptionWhenDocumentIdIsBlank() {
    assertThatThrownBy(() -> documentValidator.validateDocumentId("   "))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Document ID is required");
  }

  @Test
  void shouldThrowExceptionWhenDocumentIdIsEmpty() {
    assertThatThrownBy(() -> documentValidator.validateDocumentId(""))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Document ID is required");
  }

  @Test
  void shouldThrowExceptionWhenDocumentIdIsNotValidUUID() {
    assertThatThrownBy(() -> documentValidator.validateDocumentId("invalid-uuid"))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Invalid document ID format");
  }

  @Test
  void shouldThrowExceptionWhenDocumentIdHasWrongFormat() {
    assertThatThrownBy(() -> documentValidator.validateDocumentId("12345678-1234-1234-1234"))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Invalid document ID format");
  }

  @Test
  void shouldThrowExceptionWhenDocumentIdHasInvalidCharacters() {
    assertThatThrownBy(
            () -> documentValidator.validateDocumentId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Invalid document ID format");
  }

  @Test
  void shouldPassValidationForUppercaseUUID() {
    String validUuid = "123E4567-E89B-12D3-A456-426614174000";
    assertThatCode(() -> documentValidator.validateDocumentId(validUuid))
        .doesNotThrowAnyException();
  }
}
