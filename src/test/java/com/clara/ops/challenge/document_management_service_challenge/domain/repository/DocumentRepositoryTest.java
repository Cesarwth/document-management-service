package com.clara.ops.challenge.document_management_service_challenge.domain.repository;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.TagEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DocumentRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private DocumentRepository documentRepository;

  private DocumentEntity document1;
  private DocumentEntity document2;

  @BeforeEach
  void setUp() {
    // Create test documents
    document1 =
        DocumentEntity.builder()
            .userName("user1")
            .documentName("doc1.pdf")
            .minioPath("user1/doc1.pdf")
            .fileSize(1024L)
            .fileType("application/pdf")
            .build();

    TagEntity tag1 = TagEntity.builder().tagName("tag1").build();
    TagEntity tag2 = TagEntity.builder().tagName("tag2").build();
    document1.addTag(tag1);
    document1.addTag(tag2);

    document2 =
        DocumentEntity.builder()
            .userName("user2")
            .documentName("doc2.pdf")
            .minioPath("user2/doc2.pdf")
            .fileSize(2048L)
            .fileType("application/pdf")
            .build();

    TagEntity tag3 = TagEntity.builder().tagName("tag3").build();
    document2.addTag(tag3);

    entityManager.persist(document1);
    entityManager.persist(document2);
    entityManager.flush();
  }

  @Test
  void shouldReturnAllDocumentsWhenFindingAll() {
    List<DocumentEntity> documents = documentRepository.findAll();

    assertThat(documents).hasSize(2);
  }

  @Test
  void shouldSaveNewDocumentSuccessfully() {
    DocumentEntity newDoc =
        DocumentEntity.builder()
            .userName("user3")
            .documentName("doc3.pdf")
            .minioPath("user3/doc3.pdf")
            .fileSize(3072L)
            .fileType("application/pdf")
            .build();

    TagEntity tag = TagEntity.builder().tagName("newtag").build();
    newDoc.addTag(tag);

    DocumentEntity saved = documentRepository.save(newDoc);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUserName()).isEqualTo("user3");
    assertThat(saved.getTags()).hasSize(1);
  }

  @Test
  void shouldDeleteDocumentSuccessfullyWhenIdExists() {
    documentRepository.deleteById(document1.getId());
    entityManager.flush();

    List<DocumentEntity> documents = documentRepository.findAll();
    assertThat(documents).hasSize(1);
    assertThat(documents.get(0).getId()).isEqualTo(document2.getId());
  }

  @Test
  void shouldDeleteTagsCascadinglyWhenDeletingDocument() {
    int initialTagCount =
        entityManager
            .getEntityManager()
            .createQuery("SELECT t FROM TagEntity t", TagEntity.class)
            .getResultList()
            .size();

    documentRepository.deleteById(document1.getId());
    entityManager.flush();

    int finalTagCount =
        entityManager
            .getEntityManager()
            .createQuery("SELECT t FROM TagEntity t", TagEntity.class)
            .getResultList()
            .size();

    assertThat(finalTagCount).isLessThan(initialTagCount);
  }
}
