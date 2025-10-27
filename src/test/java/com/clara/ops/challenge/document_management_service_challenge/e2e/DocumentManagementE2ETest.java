package com.clara.ops.challenge.document_management_service_challenge.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.MinioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DocumentManagementE2ETest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private DocumentRepository documentRepository;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MinioService minioService;

  private String baseUrl;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port + "/document-management";
    documentRepository.deleteAll();

    org.mockito.Mockito.doNothing()
        .when(minioService)
        .uploadFile(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyLong());
    org.mockito.Mockito.when(
            minioService.generatePresignedUrl(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn("http://localhost:9000/documents/presigned-url?expires=3600");
  }

  @AfterEach
  void tearDown() {
    documentRepository.deleteAll();
  }

  @Test
  void shouldUploadPdfWithAllMetadataAsPerReadme() throws Exception {
    MultiValueMap<String, Object> body =
        createMultipartUploadRequest("invoice.pdf", "john-doe", Arrays.asList("finance", "legal"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<Void> response =
        restTemplate.postForEntity(baseUrl + "/upload", requestEntity, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var documents = documentRepository.findAll();
    assertThat(documents).hasSize(1);
    assertThat(documents.get(0).getUserName()).isEqualTo("john-doe");
    assertThat(documents.get(0).getDocumentName()).isEqualTo("invoice.pdf");
    assertThat(documents.get(0).getTags()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(documents.get(0).getMinioPath()).isEqualTo("john-doe/invoice.pdf");
  }

  @Test
  void shouldStoreFileInMinioWithCorrectPathStructure() throws Exception {
    MultiValueMap<String, Object> body =
        createMultipartUploadRequest("contract.pdf", "user1", Arrays.asList("legal"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            baseUrl + "/upload", new HttpEntity<>(body, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var savedDoc = documentRepository.findAll().get(0);
    assertThat(savedDoc.getMinioPath())
        .isEqualTo("user1/contract.pdf")
        .describedAs("README requires MinIO path: user/document.pdf");
  }

  @Test
  void shouldPersistAllRequiredMetadataFieldsAsPerReadme() throws Exception {
    MultiValueMap<String, Object> body =
        createMultipartUploadRequest("report.pdf", "alice", Arrays.asList("report"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    restTemplate.postForEntity(baseUrl + "/upload", new HttpEntity<>(body, headers), Void.class);

    var savedDoc = documentRepository.findAll().get(0);
    assertThat(savedDoc.getUserName()).isNotNull().describedAs("README requires: User");
    assertThat(savedDoc.getDocumentName())
        .isNotNull()
        .describedAs("README requires: Document Name");
    assertThat(savedDoc.getTags()).isNotEmpty().describedAs("README requires: Tags");
    assertThat(savedDoc.getMinioPath()).isNotNull().describedAs("README requires: MinIO Path");
    assertThat(savedDoc.getFileSize()).isNotNull().describedAs("README requires: File Size");
    assertThat(savedDoc.getFileType()).isNotNull().describedAs("README requires: File Type");
    assertThat(savedDoc.getCreatedAt()).isNotNull().describedAs("README requires: Created At");
  }

  @Test
  void shouldHandleLargeFilesUpTo500MbAsPerReadme() throws Exception {
    byte[] largePdfContent = new byte[100 * 1024 * 1024];
    Arrays.fill(largePdfContent, (byte) 'A');

    MultiValueMap<String, Object> body =
        createMultipartUploadRequestWithContent(
            "large-document.pdf",
            "large-file-user",
            Arrays.asList("large", "performance"),
            largePdfContent);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            baseUrl + "/upload", new HttpEntity<>(body, headers), Void.class);

    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.CREATED)
        .describedAs("README requires handling files up to 500MB");

    var savedDoc = documentRepository.findAll().get(0);
    assertThat(savedDoc.getFileSize()).isEqualTo(largePdfContent.length);
  }

  @Test
  void shouldSearchDocumentsByUserAsPerReadme() throws Exception {
    uploadDocument("doc1.pdf", "user1", Arrays.asList("tag1"));
    uploadDocument("doc2.pdf", "user1", Arrays.asList("tag2"));
    uploadDocument("doc3.pdf", "user2", Arrays.asList("tag3"));

    DocumentSearchFilters filters = DocumentSearchFilters.builder().user("user1").build();

    ResponseEntity<PaginatedDocumentSearchResponse> response =
        restTemplate.postForEntity(
            baseUrl + "/search?page=0&size=10", filters, PaginatedDocumentSearchResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getDocuments()).hasSize(2);
    assertThat(response.getBody().getDocuments())
        .allMatch(doc -> doc.getUser().equals("user1"))
        .describedAs("README: Filter by user");
  }

  @Test
  void shouldSearchDocumentsByNameAsPerReadme() throws Exception {
    uploadDocument("contract.pdf", "user1", Arrays.asList("legal"));
    uploadDocument("invoice.pdf", "user1", Arrays.asList("finance"));
    uploadDocument("contract-2024.pdf", "user1", Arrays.asList("legal"));

    DocumentSearchFilters filters = DocumentSearchFilters.builder().name("contract").build();

    ResponseEntity<PaginatedDocumentSearchResponse> response =
        restTemplate.postForEntity(
            baseUrl + "/search?page=0&size=10", filters, PaginatedDocumentSearchResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getDocuments()).hasSize(2);
  }

  @Test
  void shouldReturnAllDocumentsWhenNoFiltersAsPerReadme() throws Exception {
    uploadDocument("doc1.pdf", "user1", Arrays.asList("tag1"));
    uploadDocument("doc2.pdf", "user2", Arrays.asList("tag2"));
    uploadDocument("doc3.pdf", "user3", Arrays.asList("tag3"));

    DocumentSearchFilters filters = DocumentSearchFilters.builder().build();

    ResponseEntity<PaginatedDocumentSearchResponse> response =
        restTemplate.postForEntity(
            baseUrl + "/search?page=0&size=10", filters, PaginatedDocumentSearchResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getDocuments())
        .hasSize(3)
        .describedAs("README: Return all documents if no filters provided");
  }

  @Test
  void shouldSortResultsByCreatedAtDescendingAsPerReadme() throws Exception {
    uploadDocument("doc1.pdf", "user1", Arrays.asList("tag1"));
    Thread.sleep(100);
    uploadDocument("doc2.pdf", "user1", Arrays.asList("tag2"));
    Thread.sleep(100);
    uploadDocument("doc3.pdf", "user1", Arrays.asList("tag3"));

    DocumentSearchFilters filters = DocumentSearchFilters.builder().build();

    ResponseEntity<PaginatedDocumentSearchResponse> response =
        restTemplate.postForEntity(
            baseUrl + "/search?page=0&size=10", filters, PaginatedDocumentSearchResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<DocumentDto> docs = response.getBody().getDocuments();
    assertThat(docs.get(0).getName())
        .isEqualTo("doc3.pdf")
        .describedAs("README: Results ordered by created_at DESC");
  }

  @Test
  void shouldSupportPaginationAsPerReadme() throws Exception {
    for (int i = 1; i <= 15; i++) {
      uploadDocument("doc" + i + ".pdf", "paginated-user", Arrays.asList("page-test"));
    }

    DocumentSearchFilters filters = DocumentSearchFilters.builder().user("paginated-user").build();

    ResponseEntity<PaginatedDocumentSearchResponse> page0 =
        restTemplate.postForEntity(
            baseUrl + "/search?page=0&size=10", filters, PaginatedDocumentSearchResponse.class);

    assertThat(page0.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(page0.getBody().getDocuments())
        .hasSize(10)
        .describedAs("README: Support pagination with page and size");
    assertThat(page0.getBody().getMetadata().getCurrentPage()).isEqualTo(0);
    assertThat(page0.getBody().getMetadata().getTotalPages()).isEqualTo(2);
  }

  @Test
  void shouldGenerateTemporaryDownloadUrlAsPerReadme() throws Exception {
    String documentId = uploadDocument("download-test.pdf", "user1", Arrays.asList("test"));

    ResponseEntity<DocumentDownloadUrlResponse> response =
        restTemplate.getForEntity(
            baseUrl + "/download/" + documentId, DocumentDownloadUrlResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getUrl())
        .isNotNull()
        .contains("presigned-url")
        .describedAs("README: Generate temporary download URL");
  }

  @Test
  void shouldHandleConcurrentUploadsAsPerReadme() throws InterruptedException {
    int concurrentUploads = 10;
    ExecutorService executor = Executors.newFixedThreadPool(concurrentUploads);
    CountDownLatch latch = new CountDownLatch(concurrentUploads);
    AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 0; i < concurrentUploads; i++) {
      int docNum = i;
      executor.submit(
          () -> {
            try {
              MultiValueMap<String, Object> body =
                  createMultipartUploadRequest(
                      "concurrent-doc-" + docNum + ".pdf",
                      "concurrent-user-" + docNum,
                      Arrays.asList("concurrent"));

              HttpHeaders headers = new HttpHeaders();
              headers.setContentType(MediaType.MULTIPART_FORM_DATA);

              ResponseEntity<Void> response =
                  restTemplate.postForEntity(
                      baseUrl + "/upload", new HttpEntity<>(body, headers), Void.class);

              if (response.getStatusCode() == HttpStatus.CREATED) {
                successCount.incrementAndGet();
              }
            } catch (Exception e) {
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await();
    executor.shutdown();

    assertThat(successCount.get())
        .isGreaterThanOrEqualTo(8)
        .describedAs("README: System must handle 10 concurrent uploads (allowing some contention)");
  }

  private MultiValueMap<String, Object> createMultipartUploadRequest(
      String fileName, String user, List<String> tags) throws Exception {
    byte[] pdfContent = "PDF Content Here".getBytes(StandardCharsets.UTF_8);
    return createMultipartUploadRequestWithContent(fileName, user, tags, pdfContent);
  }

  private MultiValueMap<String, Object> createMultipartUploadRequestWithContent(
      String fileName, String user, List<String> tags, byte[] pdfContent) throws Exception {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    UploadDocumentRequest metadata =
        UploadDocumentRequest.builder().name(fileName).user(user).tags(tags).build();

    HttpHeaders jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> metadataEntity =
        new HttpEntity<>(objectMapper.writeValueAsString(metadata), jsonHeaders);
    body.add("metadata", metadataEntity);

    ByteArrayResource fileResource =
        new ByteArrayResource(pdfContent) {
          @Override
          public String getFilename() {
            return fileName;
          }
        };
    body.add("file", fileResource);

    return body;
  }

  private String uploadDocument(String name, String user, List<String> tags) throws Exception {
    MultiValueMap<String, Object> body = createMultipartUploadRequest(name, user, tags);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    restTemplate.postForEntity(baseUrl + "/upload", new HttpEntity<>(body, headers), Void.class);

    var documents = documentRepository.findAll();
    return documents.get(documents.size() - 1).getId().toString();
  }
}
