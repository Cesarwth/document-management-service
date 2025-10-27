package com.clara.ops.challenge.document_management_service_challenge.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {

  private Integer currentPage;

  private Integer itemsPerPage;

  private Integer currentItems;

  private Integer totalPages;

  private Long totalItems;
}
