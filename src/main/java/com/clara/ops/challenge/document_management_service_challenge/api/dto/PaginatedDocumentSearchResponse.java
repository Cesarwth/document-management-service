package com.clara.ops.challenge.document_management_service_challenge.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDocumentSearchResponse {

  private Metadata metadata;

  private List<DocumentDto> documents;
}
