package com.clara.ops.challenge.document_management_service_challenge.domain.enums;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {
  PDF("application/pdf", ".pdf");

  private final String contentType;
  private final String extension;

  public boolean matches(String contentType) {
    return Optional.ofNullable(contentType).map(this.contentType::equals).orElse(false);
  }

  public boolean hasExtension(String filename) {
    return Optional.ofNullable(filename)
        .map(String::toLowerCase)
        .map(fn -> fn.endsWith(this.extension))
        .orElse(false);
  }
}
