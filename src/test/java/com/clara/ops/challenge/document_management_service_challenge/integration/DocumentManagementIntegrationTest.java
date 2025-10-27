package com.clara.ops.challenge.document_management_service_challenge.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.clara.ops.challenge.document_management_service_challenge.service.MinioService;
import io.minio.MinioClient;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentManagementIntegrationTest {

  @Autowired private DocumentService documentService;

  @Autowired private DocumentRepository documentRepository;

  @MockitoBean private MinioClient minioClient;

  @MockitoBean private MinioService minioService;

  private UploadDocumentRequest uploadRequest;
  private MultipartFile mockFile;

  @BeforeEach
  void setUp() {
    documentRepository.deleteAll();

    uploadRequest =
        UploadDocumentRequest.builder()
            .name("integration-test.pdf")
            .user("integration-user")
            .tags(Arrays.asList("integration", "test", "spring"))
            .build();

    mockFile =
        new MockMultipartFile(
            "file", "integration-test.pdf", "application/pdf", "PDF content".getBytes());

    doNothing().when(minioService).uploadFile(any(), anyString(), anyString(), anyLong());
    when(minioService.generatePresignedUrl(anyString()))
        .thenReturn("http://localhost:9000/presigned-url");
  }

  @Test
  void shouldPersistDocumentWithMetadataAndTagsWhenUploading() {
    documentService.uploadDocument(uploadRequest, mockFile);

    List<DocumentEntity> documents = documentRepository.findAll();
    assertThat(documents).hasSize(1);

    DocumentEntity savedDoc = documents.get(0);
    assertThat(savedDoc.getUserName()).isEqualTo("integration-user");
    assertThat(savedDoc.getDocumentName()).isEqualTo("integration-test.pdf");
    assertThat(savedDoc.getFileSize()).isEqualTo(mockFile.getSize());
    assertThat(savedDoc.getFileType()).isEqualTo("application/pdf");
    assertThat(savedDoc.getTags()).hasSize(3);
    assertThat(savedDoc.getTags())
        .extracting("tagName")
        .containsExactlyInAnyOrder("integration", "test", "spring");
  }

  @Test
  void shouldReturnFilteredDocumentsSortedByDateWhenSearchingByUser() throws InterruptedException {
    UploadDocumentRequest doc1 =
        UploadDocumentRequest.builder()
            .name("doc1.pdf")
            .user("user1")
            .tags(Arrays.asList("tag1", "common"))
            .build();

    UploadDocumentRequest doc2 =
        UploadDocumentRequest.builder()
            .name("doc2.pdf")
            .user("user1")
            .tags(Arrays.asList("tag2", "common"))
            .build();

    UploadDocumentRequest doc3 =
        UploadDocumentRequest.builder()
            .name("doc3.pdf")
            .user("user2")
            .tags(Arrays.asList("tag3"))
            .build();

    documentService.uploadDocument(doc1, mockFile);
    Thread.sleep(10);
    documentService.uploadDocument(doc2, mockFile);
    Thread.sleep(10);
    documentService.uploadDocument(doc3, mockFile);

    com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters
        filters =
            com.clara.ops.challenge.document_management_service_challenge.api.dto
                .DocumentSearchFilters.builder()
                .user("user1")
                .build();

    PaginatedDocumentSearchResponse response = documentService.searchDocuments(filters, 0, 10);

    assertThat(response.getDocuments()).hasSize(2);
    assertThat(response.getMetadata().getTotalItems()).isEqualTo(2);

    List<DocumentDto> docs = response.getDocuments();
    assertThat(docs.get(0).getName()).isEqualTo("doc2.pdf");
    assertThat(docs.get(1).getName()).isEqualTo("doc1.pdf");
  }

  @Test
  void shouldReturnDocumentsMatchingTagsWhenSearchingByTags() {
    UploadDocumentRequest doc1 =
        UploadDocumentRequest.builder()
            .name("doc1.pdf")
            .user("user1")
            .tags(Arrays.asList("java", "spring"))
            .build();

    UploadDocumentRequest doc2 =
        UploadDocumentRequest.builder()
            .name("doc2.pdf")
            .user("user1")
            .tags(Arrays.asList("java", "microservices"))
            .build();

    UploadDocumentRequest doc3 =
        UploadDocumentRequest.builder()
            .name("doc3.pdf")
            .user("user1")
            .tags(Arrays.asList("python"))
            .build();

    documentService.uploadDocument(doc1, mockFile);
    documentService.uploadDocument(doc2, mockFile);
    documentService.uploadDocument(doc3, mockFile);

    com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters
        filters =
            com.clara.ops.challenge.document_management_service_challenge.api.dto
                .DocumentSearchFilters.builder()
                .tags(Arrays.asList("java"))
                .build();

    PaginatedDocumentSearchResponse response = documentService.searchDocuments(filters, 0, 10);

    assertThat(response.getDocuments()).hasSize(2);
    assertThat(response.getDocuments())
        .extracting(DocumentDto::getName)
        .containsExactlyInAnyOrder("doc1.pdf", "doc2.pdf");
  }

  @Test
  void shouldReturnPresignedUrlWhenDownloadingDocument() {
    documentService.uploadDocument(uploadRequest, mockFile);

    List<DocumentEntity> documents = documentRepository.findAll();
    String documentId = documents.get(0).getId().toString();

    var downloadResponse = documentService.getDownloadUrl(documentId);

    assertThat(downloadResponse.getUrl()).isNotNull();

    verify(minioService, times(1)).generatePresignedUrl(anyString());
  }

  @Test
  void shouldThrowExceptionWhenDownloadingNonExistentDocument() {
    String nonExistentId = "00000000-0000-0000-0000-000000000000";

    assertThatThrownBy(() -> documentService.getDownloadUrl(nonExistentId))
        .isInstanceOf(DocumentNotFoundException.class)
        .hasMessageContaining("Document not found");
  }

  @Test
  void shouldReturnCorrectPaginatedResultsWhenSearchingWithPagination() {
    for (int i = 1; i <= 15; i++) {
      UploadDocumentRequest doc =
          UploadDocumentRequest.builder()
              .name("doc" + i + ".pdf")
              .user("paginated-user")
              .tags(Arrays.asList("page-test"))
              .build();
      documentService.uploadDocument(doc, mockFile);
    }

    com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters
        filters =
            com.clara.ops.challenge.document_management_service_challenge.api.dto
                .DocumentSearchFilters.builder()
                .user("paginated-user")
                .build();

    PaginatedDocumentSearchResponse page0 = documentService.searchDocuments(filters, 0, 10);

    assertThat(page0.getDocuments()).hasSize(10);
    assertThat(page0.getMetadata().getCurrentPage()).isEqualTo(0);
    assertThat(page0.getMetadata().getTotalPages()).isEqualTo(2);
    assertThat(page0.getMetadata().getTotalItems()).isEqualTo(15);
    assertThat(page0.getMetadata().getCurrentItems()).isEqualTo(10);

    PaginatedDocumentSearchResponse page1 = documentService.searchDocuments(filters, 1, 10);

    assertThat(page1.getDocuments()).hasSize(5);
    assertThat(page1.getMetadata().getCurrentPage()).isEqualTo(1);
    assertThat(page1.getMetadata().getCurrentItems()).isEqualTo(5);
  }

  @Test
  void shouldFilterOutEmptyTagsWhenUploadingDocument() {
    UploadDocumentRequest docWithEmptyTags =
        UploadDocumentRequest.builder()
            .name("doc-with-empty-tags.pdf")
            .user("user1")
            .tags(Arrays.asList("valid-tag", "", "  ", "another-valid-tag"))
            .build();

    documentService.uploadDocument(docWithEmptyTags, mockFile);

    List<DocumentEntity> documents = documentRepository.findAll();

    assertThat(documents.get(0).getTags()).hasSize(2);
    assertThat(documents.get(0).getTags())
        .extracting("tagName")
        .containsExactlyInAnyOrder("valid-tag", "another-valid-tag");
  }

  @Test
  void shouldReturnDocumentMatchingAllCriteriaWhenSearchingWithCombinedFilters() {
    UploadDocumentRequest doc1 =
        UploadDocumentRequest.builder()
            .name("contract.pdf")
            .user("john")
            .tags(Arrays.asList("legal", "2024"))
            .build();

    UploadDocumentRequest doc2 =
        UploadDocumentRequest.builder()
            .name("invoice.pdf")
            .user("john")
            .tags(Arrays.asList("finance", "2024"))
            .build();

    UploadDocumentRequest doc3 =
        UploadDocumentRequest.builder()
            .name("contract.pdf")
            .user("jane")
            .tags(Arrays.asList("legal", "2024"))
            .build();

    documentService.uploadDocument(doc1, mockFile);
    documentService.uploadDocument(doc2, mockFile);
    documentService.uploadDocument(doc3, mockFile);

    com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters
        filters =
            com.clara.ops.challenge.document_management_service_challenge.api.dto
                .DocumentSearchFilters.builder()
                .user("john")
                .name("contract")
                .tags(Arrays.asList("legal"))
                .build();

    PaginatedDocumentSearchResponse response = documentService.searchDocuments(filters, 0, 10);

    assertThat(response.getDocuments()).hasSize(1);
    assertThat(response.getDocuments().get(0).getName()).isEqualTo("contract.pdf");
    assertThat(response.getDocuments().get(0).getUser()).isEqualTo("john");
  }
}
