package com.clara.ops.challenge.document_management_service_challenge.mapper;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.TagEntity;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentMapperTest {

  private DocumentMapper documentMapper;

  @BeforeEach
  void setUp() {
    documentMapper = new DocumentMapper();
  }

  @Test
  void shouldMapEntityToDtoSuccessfully() {
    UUID id = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    DocumentEntity entity = createDocumentEntity(id, now, Arrays.asList("tag1", "tag2"));

    DocumentDto dto = documentMapper.toDto(entity);

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(id.toString());
    assertThat(dto.getUser()).isEqualTo("testuser");
    assertThat(dto.getName()).isEqualTo("test.pdf");
    assertThat(dto.getSize()).isEqualTo(1024L);
    assertThat(dto.getType()).isEqualTo("application/pdf");
    assertThat(dto.getTags()).containsExactly("tag1", "tag2");
    assertThat(dto.getCreatedAt()).isNotNull();
  }

  @Test
  void shouldThrowExceptionWhenEntityIsNull() {
    assertThatThrownBy(() -> documentMapper.toDto(null))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Cannot map null entity");
  }

  @Test
  void shouldThrowExceptionWhenIdIsNull() {
    DocumentEntity entity = createDocumentEntity(null, LocalDateTime.now(), Arrays.asList("tag1"));

    assertThatThrownBy(() -> documentMapper.toDto(entity))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Document ID cannot be null");
  }

  @Test
  void shouldThrowExceptionWhenUserNameIsNull() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), Arrays.asList("tag1"));
    entity.setUserName(null);

    assertThatThrownBy(() -> documentMapper.toDto(entity))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("User name cannot be null");
  }

  @Test
  void shouldThrowExceptionWhenDocumentNameIsNull() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), Arrays.asList("tag1"));
    entity.setDocumentName(null);

    assertThatThrownBy(() -> documentMapper.toDto(entity))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Document name cannot be null");
  }

  @Test
  void shouldThrowExceptionWhenFileSizeIsNull() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), Arrays.asList("tag1"));
    entity.setFileSize(null);

    assertThatThrownBy(() -> documentMapper.toDto(entity))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File size cannot be null");
  }

  @Test
  void shouldThrowExceptionWhenFileTypeIsNull() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), Arrays.asList("tag1"));
    entity.setFileType(null);

    assertThatThrownBy(() -> documentMapper.toDto(entity))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("File type cannot be null");
  }

  @Test
  void shouldThrowExceptionWhenCreatedAtIsNull() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, null, Arrays.asList("tag1"));

    assertThatThrownBy(() -> documentMapper.toDto(entity))
        .isInstanceOf(InvalidDocumentException.class)
        .hasMessageContaining("Created date cannot be null");
  }

  @Test
  void shouldReturnEmptyListWhenTagsAreNull() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), null);

    DocumentDto dto = documentMapper.toDto(entity);

    assertThat(dto.getTags()).isNotNull().isEmpty();
  }

  @Test
  void shouldFilterOutNullTagNames() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), null);

    List<TagEntity> tags = new ArrayList<>();
    tags.add(createTagEntity("validTag"));
    tags.add(createTagEntity(null));
    tags.add(createTagEntity("anotherTag"));
    entity.setTags(tags);

    DocumentDto dto = documentMapper.toDto(entity);

    assertThat(dto.getTags()).containsExactly("validTag", "anotherTag");
  }

  @Test
  void shouldFilterOutBlankTagNames() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), null);

    List<TagEntity> tags = new ArrayList<>();
    tags.add(createTagEntity("validTag"));
    tags.add(createTagEntity("   "));
    tags.add(createTagEntity(""));
    tags.add(createTagEntity("anotherTag"));
    entity.setTags(tags);

    DocumentDto dto = documentMapper.toDto(entity);

    assertThat(dto.getTags()).containsExactly("validTag", "anotherTag");
  }

  @Test
  void shouldMapEntityWithEmptyTagsList() {
    UUID id = UUID.randomUUID();
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), new ArrayList<>());

    DocumentDto dto = documentMapper.toDto(entity);

    assertThat(dto.getTags()).isNotNull().isEmpty();
  }

  @Test
  void shouldMapEntityWithManyTags() {
    UUID id = UUID.randomUUID();
    List<String> tagNames = Arrays.asList("tag1", "tag2", "tag3", "tag4", "tag5");
    DocumentEntity entity = createDocumentEntity(id, LocalDateTime.now(), tagNames);

    DocumentDto dto = documentMapper.toDto(entity);

    assertThat(dto.getTags()).containsExactlyElementsOf(tagNames);
  }

  private DocumentEntity createDocumentEntity(
      UUID id, LocalDateTime createdAt, List<String> tagNames) {
    DocumentEntity entity = new DocumentEntity();
    entity.setId(id);
    entity.setUserName("testuser");
    entity.setDocumentName("test.pdf");
    entity.setMinioPath("testuser/test.pdf");
    entity.setFileSize(1024L);
    entity.setFileType("application/pdf");
    entity.setCreatedAt(createdAt);
    entity.setUpdatedAt(createdAt);

    if (tagNames != null) {
      List<TagEntity> tags = new ArrayList<>();
      for (String tagName : tagNames) {
        tags.add(createTagEntity(tagName));
      }
      entity.setTags(tags);
    }

    return entity;
  }

  private TagEntity createTagEntity(String tagName) {
    TagEntity tag = new TagEntity();
    tag.setTagName(tagName);
    return tag;
  }
}
