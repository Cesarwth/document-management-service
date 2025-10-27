package com.clara.ops.challenge.document_management_service_challenge.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDocumentSearchResponse {

  private Metadata metadata;

  private List<DocumentDto> documents;
}
