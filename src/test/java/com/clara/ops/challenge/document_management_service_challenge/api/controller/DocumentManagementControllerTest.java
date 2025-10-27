package com.clara.ops.challenge.document_management_service_challenge.api.controller;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.api.dto.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.config.DocumentManagementProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.clara.ops.challenge.document_management_service_challenge.validation.DocumentValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentManagementController.class)
@Import(DocumentManagementProperties.class)
class DocumentManagementControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private DocumentService documentService;

  @MockitoBean private DocumentValidator documentValidator;

  private UploadDocumentRequest uploadRequest;
  private DocumentSearchFilters searchFilters;
  private PaginatedDocumentSearchResponse searchResponse;
  private DocumentDownloadUrlResponse downloadResponse;

  @BeforeEach
  void setUp() {
    uploadRequest =
        UploadDocumentRequest.builder()
            .user("testuser")
            .name("testdoc.pdf")
            .tags(Arrays.asList("tag1", "tag2"))
            .build();

    searchFilters = DocumentSearchFilters.builder().user("testuser").build();

    DocumentDto documentDto =
        DocumentDto.builder()
            .id("123e4567-e89b-12d3-a456-426614174000")
            .user("testuser")
            .name("testdoc.pdf")
            .tags(Arrays.asList("tag1", "tag2"))
            .size(1024L)
            .type("application/pdf")
            .createdAt("2024-01-01T10:00:00")
            .build();

    Metadata metadata =
        Metadata.builder()
            .currentPage(0)
            .itemsPerPage(20)
            .currentItems(1)
            .totalPages(1)
            .totalItems(1L)
            .build();

    searchResponse =
        PaginatedDocumentSearchResponse.builder()
            .metadata(metadata)
            .documents(Arrays.asList(documentDto))
            .build();

    downloadResponse =
        DocumentDownloadUrlResponse.builder()
            .url("http://localhost:9000/bucket/testuser/testdoc.pdf?presigned=true")
            .build();
  }

  @Test
  void shouldReturnCreatedStatusWhenUploadingValidDocument() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());

    MockMultipartFile metadata =
        new MockMultipartFile(
            "metadata",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(uploadRequest));

    doNothing().when(documentService).uploadDocument(any(), any());

    mockMvc
        .perform(multipart("/document-management/upload").file(file).file(metadata))
        .andExpect(status().isCreated());

    verify(documentService, times(1)).uploadDocument(any(), any());
  }

  @Test
  void shouldReturnSearchResultsWhenSearchingDocuments() throws Exception {
    when(documentService.searchDocuments(any(), anyInt(), anyInt())).thenReturn(searchResponse);

    mockMvc
        .perform(
            post("/document-management/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchFilters))
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metadata.totalItems").value(1))
        .andExpect(jsonPath("$.documents[0].user").value("testuser"));

    verify(documentService, times(1)).searchDocuments(any(), eq(0), eq(20));
  }

  @Test
  void shouldReturnDownloadUrlWhenDocumentIdIsValid() throws Exception {
    String documentId = "123e4567-e89b-12d3-a456-426614174000";
    when(documentService.getDownloadUrl(documentId)).thenReturn(downloadResponse);

    mockMvc
        .perform(get("/document-management/download/{documentId}", documentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.url").exists());

    verify(documentService, times(1)).getDownloadUrl(documentId);
  }

  @Test
  void shouldReturnBadRequestWhenUploadingInvalidFileType() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

    MockMultipartFile metadata =
        new MockMultipartFile(
            "metadata",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(uploadRequest));

    // Configure validator to throw exception for invalid file
    doThrow(new InvalidDocumentException("Invalid file type"))
        .when(documentValidator)
        .validateFile(any());

    mockMvc
        .perform(multipart("/document-management/upload").file(file).file(metadata))
        .andExpect(status().isBadRequest());

    verify(documentService, never()).uploadDocument(any(), any());
  }

  @Test
  void shouldReturnBadRequestWhenDownloadingWithInvalidId() throws Exception {
    String documentId = "invalid-id";

    // Configure validator to throw exception for invalid document ID
    doThrow(new InvalidDocumentException("Invalid document ID format"))
        .when(documentValidator)
        .validateDocumentId(documentId);

    mockMvc
        .perform(get("/document-management/download/{documentId}", documentId))
        .andExpect(status().isBadRequest());

    verify(documentService, never()).getDownloadUrl(anyString());
  }

  @Test
  void shouldReturnNotFoundWhenDocumentDoesNotExist() throws Exception {
    String documentId = "123e4567-e89b-12d3-a456-426614174000";
    when(documentService.getDownloadUrl(documentId))
        .thenThrow(new DocumentNotFoundException("Document not found"));

    mockMvc
        .perform(get("/document-management/download/{documentId}", documentId))
        .andExpect(status().isNotFound());
  }
}
