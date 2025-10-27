package com.clara.ops.challenge.document_management_service_challenge.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentRequest {

  @NotBlank(message = "User is required")
  private String user;

  @NotBlank(message = "Document name is required")
  private String name;

  @NotNull(message = "Tags are required") @NotEmpty(message = "At least one tag is required")
  private List<String> tags;
}
