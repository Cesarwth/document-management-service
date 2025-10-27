package com.clara.ops.challenge.document_management_service_challenge.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DocumentEntityTest {

  private DocumentEntity document;

  @BeforeEach
  void setUp() {
    document = new DocumentEntity();
  }

  @Test
  void shouldAddTagSuccessfully() {
    TagEntity tag = new TagEntity();
    tag.setTagName("test-tag");

    document.addTag(tag);

    assertThat(document.getTags()).hasSize(1).contains(tag);
  }

  @Test
  void shouldNotAddNullTag() {
    document.addTag(null);

    assertThat(document.getTags()).isEmpty();
  }

  @Test
  void shouldAddMultipleTags() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("tag1");
    TagEntity tag2 = new TagEntity();
    tag2.setTagName("tag2");
    TagEntity tag3 = new TagEntity();
    tag3.setTagName("tag3");

    document.addTag(tag1);
    document.addTag(tag2);
    document.addTag(tag3);

    assertThat(document.getTags()).hasSize(3).containsExactly(tag1, tag2, tag3);
  }

  @Test
  void shouldInitializeTagsListWhenNull() {
    TagEntity tag = new TagEntity();
    tag.setTagName("test-tag");
    document.addTag(tag);

    assertThat(document.getTags()).isNotNull().hasSize(1).contains(tag);
  }

  @Test
  void shouldRemoveTagSuccessfully() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("tag1");
    TagEntity tag2 = new TagEntity();
    tag2.setTagName("tag2");

    document.addTag(tag1);
    document.addTag(tag2);
    assertThat(document.getTags()).hasSize(2);

    document.removeTag(tag1);

    assertThat(document.getTags()).hasSize(1).containsOnly(tag2);
  }

  @Test
  void shouldNotRemoveNullTag() {
    TagEntity tag = new TagEntity();
    tag.setTagName("test-tag");
    document.addTag(tag);

    document.removeTag(null);

    assertThat(document.getTags()).hasSize(1).contains(tag);
  }

  @Test
  void shouldHandleRemoveTagWhenTagsListIsNull() {
    TagEntity tag = new TagEntity();
    tag.setTagName("test-tag");

    assertThatCode(() -> document.removeTag(tag)).doesNotThrowAnyException();
  }

  @Test
  void shouldHandleRemoveTagWhenTagsListIsEmpty() {
    document.setTags(new ArrayList<>());
    TagEntity tag = new TagEntity();
    tag.setTagName("test-tag");

    assertThatCode(() -> document.removeTag(tag)).doesNotThrowAnyException();

    assertThat(document.getTags()).isEmpty();
  }

  @Test
  void shouldRemoveSpecificTagInstance() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("tag1");
    TagEntity tag2 = new TagEntity();
    tag2.setTagName("tag2");

    document.addTag(tag1);
    document.addTag(tag2);

    document.removeTag(tag1);

    assertThat(document.getTags()).hasSize(1);
  }

  @Test
  void shouldSetAndGetAllProperties() {
    java.util.UUID id = java.util.UUID.randomUUID();
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    List<TagEntity> tags = new ArrayList<>();

    document.setId(id);
    document.setUserName("testuser");
    document.setDocumentName("test.pdf");
    document.setMinioPath("testuser/test.pdf");
    document.setFileSize(1024L);
    document.setFileType("application/pdf");
    document.setCreatedAt(now);
    document.setUpdatedAt(now);
    document.setTags(tags);

    assertThat(document.getId()).isEqualTo(id);
    assertThat(document.getUserName()).isEqualTo("testuser");
    assertThat(document.getDocumentName()).isEqualTo("test.pdf");
    assertThat(document.getMinioPath()).isEqualTo("testuser/test.pdf");
    assertThat(document.getFileSize()).isEqualTo(1024L);
    assertThat(document.getFileType()).isEqualTo("application/pdf");
    assertThat(document.getCreatedAt()).isEqualTo(now);
    assertThat(document.getUpdatedAt()).isEqualTo(now);
    assertThat(document.getTags()).isEqualTo(tags);
  }
}
