package com.clara.ops.challenge.document_management_service_challenge.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TagEntityTest {

  @Test
  void shouldReturnTrueForEqualTagsWithSameTagName() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("testTag");

    TagEntity tag2 = new TagEntity();
    tag2.setTagName("testTag");

    assertThat(tag1).isEqualTo(tag2);
    assertThat(tag1.hashCode()).isEqualTo(tag2.hashCode());
  }

  @Test
  void shouldReturnFalseForTagsWithDifferentTagNames() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("tag1");

    TagEntity tag2 = new TagEntity();
    tag2.setTagName("tag2");

    assertThat(tag1).isNotEqualTo(tag2);
  }

  @Test
  void shouldReturnFalseWhenComparingWithNull() {
    TagEntity tag = new TagEntity();
    tag.setTagName("testTag");

    assertThat(tag.equals(null)).isFalse();
  }

  @Test
  void shouldReturnTrueWhenComparingWithSameInstance() {
    TagEntity tag = new TagEntity();
    tag.setTagName("testTag");

    assertThat(tag).isEqualTo(tag);
  }

  @Test
  void shouldReturnFalseWhenComparingWithDifferentClass() {
    TagEntity tag = new TagEntity();
    tag.setTagName("testTag");

    assertThat(tag.equals("testTag")).isFalse();
  }

  @Test
  void shouldReturnFalseWhenBothTagNamesAreNull() {
    TagEntity tag1 = new TagEntity();
    TagEntity tag2 = new TagEntity();

    assertThat(tag1).isNotEqualTo(tag2);
  }

  @Test
  void shouldReturnFalseWhenOneTagNameIsNull() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("testTag");

    TagEntity tag2 = new TagEntity();
    tag2.setTagName(null);

    assertThat(tag1).isNotEqualTo(tag2);
    assertThat(tag2).isNotEqualTo(tag1);
  }

  @Test
  void shouldHandleNullTagNameInHashCode() {
    TagEntity tag = new TagEntity();
    tag.setTagName(null);

    int hashCode = tag.hashCode();

    assertThat(hashCode).isEqualTo(TagEntity.class.hashCode());
  }

  @Test
  void shouldGenerateConsistentHashCodeForSameTagName() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("testTag");

    TagEntity tag2 = new TagEntity();
    tag2.setTagName("testTag");

    assertThat(tag1.hashCode()).isEqualTo(tag2.hashCode());
  }

  @Test
  void shouldGenerateDifferentHashCodeForDifferentTagNames() {
    TagEntity tag1 = new TagEntity();
    tag1.setTagName("tag1");

    TagEntity tag2 = new TagEntity();
    tag2.setTagName("tag2");

    assertThat(tag1.hashCode()).isNotEqualTo(tag2.hashCode());
  }

  @Test
  void shouldSetAndGetAllProperties() {
    Long id = 1L;
    String tagName = "testTag";
    DocumentEntity document = new DocumentEntity();

    TagEntity tag = new TagEntity();
    tag.setId(id);
    tag.setTagName(tagName);
    tag.setDocument(document);

    assertThat(tag.getId()).isEqualTo(id);
    assertThat(tag.getTagName()).isEqualTo(tagName);
    assertThat(tag.getDocument()).isEqualTo(document);
  }
}
