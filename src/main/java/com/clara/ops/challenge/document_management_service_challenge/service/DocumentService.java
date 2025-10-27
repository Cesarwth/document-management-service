package com.clara.ops.challenge.document_management_service_challenge.service;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.TagEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.enums.FileType;
import com.clara.ops.challenge.document_management_service_challenge.domain.enums.LogMessage;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentSpecification;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentUploadException;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;
import com.clara.ops.challenge.document_management_service_challenge.mapper.DocumentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final MinioService minioService;
  private final DocumentMapper documentMapper;

  /**
   * Uploads a document to MinIO and saves metadata to the database. Uses streaming to handle large
   * files efficiently without loading them entirely into memory.
   *
   * <p>Note: File validation is performed at the controller layer.
   */
  @Transactional
  public void uploadDocument(UploadDocumentRequest request, MultipartFile file) {
    Optional.ofNullable(request)
        .orElseThrow(() -> new InvalidDocumentException("Upload request cannot be null"));
    Optional.ofNullable(file)
        .orElseThrow(() -> new InvalidDocumentException("File cannot be null"));

    log.info(LogMessage.SERVICE_UPLOAD_STARTED.getMessage(), request.getName(), request.getUser());

    String documentName = ensurePdfExtension(request.getName());

    String minioPath = buildMinioPath(request.getUser(), documentName);

    try {
      InputStream inputStream = file.getInputStream();
      minioService.uploadFile(
          inputStream, minioPath, FileType.PDF.getContentType(), file.getSize());

      DocumentEntity document =
          DocumentEntity.builder()
              .userName(request.getUser())
              .documentName(documentName)
              .minioPath(minioPath)
              .fileSize(file.getSize())
              .fileType(FileType.PDF.getContentType())
              .build();

      Optional.ofNullable(request.getTags())
          .ifPresent(
              tags ->
                  tags.stream()
                      .filter(tagName -> tagName != null && !tagName.isBlank())
                      .forEach(
                          tagName -> {
                            TagEntity tag = TagEntity.builder().tagName(tagName.trim()).build();
                            document.addTag(tag);
                          }));

      documentRepository.save(document);
      log.info(LogMessage.SERVICE_UPLOAD_SUCCESS.getMessage(), document.getId());
    } catch (Exception e) {
      log.error(LogMessage.SERVICE_UPLOAD_ERROR.getMessage(), e);
      throw new DocumentUploadException("Failed to upload document", e);
    }
  }

  /**
   * Searches for documents based on filters, with pagination and sorting.
   *
   * <p>Note: Pagination validation is performed at the controller layer.
   */
  @Transactional(readOnly = true)
  public PaginatedDocumentSearchResponse searchDocuments(
      DocumentSearchFilters filters, int page, int size) {
    log.info(LogMessage.SERVICE_SEARCH_STARTED.getMessage(), filters);

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Specification<DocumentEntity> spec = DocumentSpecification.withFilters(filters);

    Page<DocumentEntity> documentPage = documentRepository.findAll(spec, pageable);

    Page<DocumentDto> dtoPage = documentPage.map(documentMapper::toDto);

    Metadata metadata =
        Metadata.builder()
            .currentPage(dtoPage.getNumber())
            .itemsPerPage(dtoPage.getSize())
            .currentItems(dtoPage.getNumberOfElements())
            .totalPages(dtoPage.getTotalPages())
            .totalItems(dtoPage.getTotalElements())
            .build();

    return PaginatedDocumentSearchResponse.builder()
        .metadata(metadata)
        .documents(dtoPage.getContent())
        .build();
  }

  /**
   * Generates a presigned download URL for a document.
   *
   * <p>Note: Document ID validation is performed at the controller layer.
   */
  @Transactional(readOnly = true)
  public DocumentDownloadUrlResponse getDownloadUrl(String documentId) {
    log.info(LogMessage.SERVICE_DOWNLOAD_STARTED.getMessage(), documentId);

    UUID uuid = UUID.fromString(documentId);

    DocumentEntity document =
        documentRepository
            .findById(uuid)
            .orElseThrow(
                () -> new DocumentNotFoundException("Document not found with id: " + documentId));

    String url = minioService.generatePresignedUrl(document.getMinioPath());

    return DocumentDownloadUrlResponse.builder().url(url).build();
  }

  private String ensurePdfExtension(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> !f.isBlank())
        .map(String::trim)
        .map(
            trimmed ->
                FileType.PDF.hasExtension(trimmed)
                    ? trimmed
                    : trimmed + FileType.PDF.getExtension())
        .orElseThrow(() -> new InvalidDocumentException("Filename cannot be empty"));
  }

  private String buildMinioPath(String user, String documentName) {
    String validUser =
        Optional.ofNullable(user)
            .filter(u -> !u.isBlank())
            .orElseThrow(() -> new InvalidDocumentException("User cannot be empty"));
    String validDocName =
        Optional.ofNullable(documentName)
            .filter(d -> !d.isBlank())
            .orElseThrow(() -> new InvalidDocumentException("Document name cannot be empty"));
    return validUser + "/" + validDocName;
  }
}
