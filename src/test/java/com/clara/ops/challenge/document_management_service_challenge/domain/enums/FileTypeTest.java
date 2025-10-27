package com.clara.ops.challenge.document_management_service_challenge.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileTypeTest {

  @Test
  void shouldMatchPdfContentType() {
    assertThat(FileType.PDF.matches("application/pdf")).isTrue();
  }

  @Test
  void shouldNotMatchNonPdfContentType() {
    assertThat(FileType.PDF.matches("text/plain")).isFalse();
    assertThat(FileType.PDF.matches("image/jpeg")).isFalse();
    assertThat(FileType.PDF.matches("application/json")).isFalse();
  }

  @Test
  void shouldNotMatchNullContentType() {
    assertThat(FileType.PDF.matches(null)).isFalse();
  }

  @Test
  void shouldHavePdfExtension() {
    assertThat(FileType.PDF.hasExtension("document.pdf")).isTrue();
    assertThat(FileType.PDF.hasExtension("test.PDF")).isTrue();
    assertThat(FileType.PDF.hasExtension("file.PdF")).isTrue();
  }

  @Test
  void shouldNotHaveNonPdfExtension() {
    assertThat(FileType.PDF.hasExtension("document.txt")).isFalse();
    assertThat(FileType.PDF.hasExtension("image.jpg")).isFalse();
    assertThat(FileType.PDF.hasExtension("data.json")).isFalse();
  }

  @Test
  void shouldNotHaveExtensionForNullFilename() {
    assertThat(FileType.PDF.hasExtension(null)).isFalse();
  }

  @Test
  void shouldNotHaveExtensionForFilenameWithoutExtension() {
    assertThat(FileType.PDF.hasExtension("document")).isFalse();
    assertThat(FileType.PDF.hasExtension("")).isFalse();
  }

  @Test
  void shouldHaveCorrectContentType() {
    assertThat(FileType.PDF.getContentType()).isEqualTo("application/pdf");
  }

  @Test
  void shouldHaveCorrectExtension() {
    assertThat(FileType.PDF.getExtension()).isEqualTo(".pdf");
  }
}
