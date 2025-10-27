package com.clara.ops.challenge.document_management_service_challenge.api.controller;

import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.config.DocumentManagementProperties;
import com.clara.ops.challenge.document_management_service_challenge.domain.enums.LogMessage;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.clara.ops.challenge.document_management_service_challenge.validation.DocumentValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/document-management")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DocumentManagementController {

  private final DocumentService documentService;
  private final DocumentValidator documentValidator;
  private final DocumentManagementProperties properties;

  /**
   * Upload a PDF document with metadata. This endpoint accepts multipart/form-data with two parts:
   * - metadata: JSON with user, name, and tags - file: The PDF file to upload
   *
   * <p>The file is streamed to storage to minimize memory usage.
   *
   * @param metadata Document metadata including user, name, and tags
   * @param file PDF file to upload
   * @return 201 Created on successful upload
   */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> uploadDocument(
      @RequestPart("metadata") @Valid UploadDocumentRequest metadata,
      @RequestPart("file") MultipartFile file) {
    log.info(
        LogMessage.UPLOAD_REQUEST_RECEIVED.getMessage(), metadata.getName(), metadata.getUser());

    documentValidator.validateFile(file);
    documentService.uploadDocument(metadata, file);

    log.info(LogMessage.UPLOAD_SUCCESS.getMessage(), metadata.getName());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * Search for documents with optional filters. Returns paginated results sorted by creation date
   * in descending order.
   *
   * @param filters Optional filters (user, name, tags)
   * @param page Page number (zero-based)
   * @param size Number of items per page
   * @return Paginated search results
   */
  @PostMapping("/search")
  public ResponseEntity<PaginatedDocumentSearchResponse> searchDocuments(
      @RequestBody @Valid DocumentSearchFilters filters,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(required = false) @Min(1) Integer size) {

    int pageSize =
        Optional.ofNullable(size).orElseGet(() -> properties.getPagination().getDefaultSize());

    log.info(LogMessage.SEARCH_REQUEST_RECEIVED.getMessage(), page, pageSize, filters);

    documentValidator.validatePaginationParams(page, pageSize);
    PaginatedDocumentSearchResponse response =
        documentService.searchDocuments(filters, page, pageSize);

    Optional.ofNullable(response)
        .map(PaginatedDocumentSearchResponse::getMetadata)
        .ifPresent(
            metadata ->
                log.info(
                    LogMessage.SEARCH_COMPLETED.getMessage(),
                    metadata.getCurrentItems(),
                    metadata.getTotalItems()));

    return ResponseEntity.ok(response);
  }

  /**
   * Get a presigned download URL for a document. The URL is temporary and will expire after the
   * configured time.
   *
   * @param documentId UUID of the document to download
   * @return Response containing the presigned download URL
   */
  @GetMapping("/download/{documentId}")
  public ResponseEntity<DocumentDownloadUrlResponse> downloadDocument(
      @PathVariable String documentId) {
    log.info(LogMessage.DOWNLOAD_REQUEST_RECEIVED.getMessage(), documentId);

    documentValidator.validateDocumentId(documentId);
    DocumentDownloadUrlResponse response = documentService.getDownloadUrl(documentId);

    log.info(LogMessage.DOWNLOAD_SUCCESS.getMessage(), documentId);
    return ResponseEntity.ok(response);
  }
}
