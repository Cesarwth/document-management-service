package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;
import com.clara.ops.challenge.document_management_service_challenge.mapper.DocumentMapper;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock private DocumentRepository documentRepository;

  @Mock private MinioService minioService;

  @Mock private DocumentMapper documentMapper;

  @InjectMocks private DocumentService documentService;

  private UploadDocumentRequest uploadRequest;
  private MockMultipartFile mockFile;
  private DocumentEntity documentEntity;
  private DocumentDto documentDto;

  @BeforeEach
  void setUp() {
    uploadRequest =
        UploadDocumentRequest.builder()
            .user("testuser")
            .name("testdoc")
            .tags(Arrays.asList("tag1", "tag2"))
            .build();

    byte[] content = "test pdf content".getBytes();
    mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", content);

    documentEntity =
        DocumentEntity.builder()
            .id(UUID.randomUUID())
            .userName("testuser")
            .documentName("testdoc.pdf")
            .minioPath("testuser/testdoc.pdf")
            .fileSize(1024L)
            .fileType("application/pdf")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    documentDto =
        DocumentDto.builder()
            .id(documentEntity.getId().toString())
            .user("testuser")
            .name("testdoc.pdf")
            .tags(Arrays.asList("tag1", "tag2"))
            .size(1024L)
            .type("application/pdf")
            .createdAt(LocalDateTime.now().toString())
            .build();
  }

  @Test
  void shouldUploadDocumentSuccessfullyWhenFileIsValid() throws Exception {
    doNothing()
        .when(minioService)
        .uploadFile(any(InputStream.class), anyString(), anyString(), anyLong());
    when(documentRepository.save(any(DocumentEntity.class))).thenReturn(documentEntity);

    documentService.uploadDocument(uploadRequest, mockFile);

    verify(minioService, times(1))
        .uploadFile(
            any(InputStream.class),
            eq("testuser/testdoc.pdf"),
            eq("application/pdf"),
            eq((long) mockFile.getSize()));
    verify(documentRepository, times(1)).save(any(DocumentEntity.class));
  }

  @Test
  void shouldThrowExceptionWhenUploadingNullFile() {
    // This test verifies service validates input and throws InvalidDocumentException for null file
    assertThatThrownBy(() -> documentService.uploadDocument(uploadRequest, null))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File cannot be null");
  }

  @Test
  void shouldUploadNonPdfFileSuccessfullyWhenValidatedByController() {
    MockMultipartFile txtFile =
        new MockMultipartFile("file", "test.txt", "application/pdf", "content".getBytes());

    when(documentRepository.save(any(DocumentEntity.class))).thenReturn(documentEntity);

    documentService.uploadDocument(uploadRequest, txtFile);

    verify(minioService, times(1))
        .uploadFile(any(InputStream.class), anyString(), anyString(), anyLong());
    verify(documentRepository, times(1)).save(any(DocumentEntity.class));
  }

  @Test
  void shouldReturnAllDocumentsWhenSearchingWithoutFilters() {
    DocumentSearchFilters filters = new DocumentSearchFilters();
    List<DocumentEntity> entities = Arrays.asList(documentEntity);
    Page<DocumentEntity> page = new PageImpl<>(entities);

    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);
    when(documentMapper.toDto(any(DocumentEntity.class))).thenReturn(documentDto);

    PaginatedDocumentSearchResponse response = documentService.searchDocuments(filters, 0, 20);

    assertThat(response).isNotNull();
    assertThat(response.getDocuments()).hasSize(1);
    assertThat(response.getMetadata().getTotalItems()).isEqualTo(1L);
    verify(documentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void shouldReturnFilteredDocumentsWhenSearchingByUser() {
    DocumentSearchFilters filters = DocumentSearchFilters.builder().user("testuser").build();
    List<DocumentEntity> entities = Arrays.asList(documentEntity);
    Page<DocumentEntity> page = new PageImpl<>(entities);

    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);
    when(documentMapper.toDto(any(DocumentEntity.class))).thenReturn(documentDto);

    PaginatedDocumentSearchResponse response = documentService.searchDocuments(filters, 0, 20);

    assertThat(response).isNotNull();
    assertThat(response.getDocuments()).hasSize(1);
    verify(documentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void shouldReturnPresignedUrlWhenDocumentIdIsValid() {
    UUID documentId = UUID.randomUUID();
    documentEntity.setId(documentId);
    String expectedUrl = "http://minio:9000/bucket/testuser/testdoc.pdf?presigned=true";

    when(documentRepository.findById(documentId)).thenReturn(Optional.of(documentEntity));
    when(minioService.generatePresignedUrl(anyString())).thenReturn(expectedUrl);

    DocumentDownloadUrlResponse response = documentService.getDownloadUrl(documentId.toString());

    assertThat(response).isNotNull();
    assertThat(response.getUrl()).isEqualTo(expectedUrl);
    verify(documentRepository, times(1)).findById(documentId);
    verify(minioService, times(1)).generatePresignedUrl(documentEntity.getMinioPath());
  }

  @Test
  void shouldThrowExceptionWhenDocumentIdIsInvalid() {
    // Service assumes valid UUID, so it throws IllegalArgumentException if invalid
    String invalidId = "invalid-uuid";

    assertThatThrownBy(() -> documentService.getDownloadUrl(invalidId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid UUID string");
  }

  @Test
  void shouldThrowExceptionWhenDocumentNotFound() {
    UUID documentId = UUID.randomUUID();
    when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentService.getDownloadUrl(documentId.toString()))
        .isInstanceOf(DocumentNotFoundException.class)
        .hasMessageContaining("Document not found");
  }
}
