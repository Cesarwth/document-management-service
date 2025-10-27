package com.clara.ops.challenge.document_management_service_challenge.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Column(name = "document_name", nullable = false, length = 500)
  private String documentName;

  @Column(name = "minio_path", nullable = false, length = 1000)
  private String minioPath;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "file_type", nullable = false, length = 100)
  private String fileType;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(
      mappedBy = "document",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @Builder.Default
  private List<TagEntity> tags = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public void addTag(TagEntity tag) {
    Optional.ofNullable(tag)
        .ifPresent(
            t -> {
              Optional.ofNullable(this.tags)
                  .orElseGet(
                      () -> {
                        this.tags = new ArrayList<>();
                        return this.tags;
                      })
                  .add(t);
              t.setDocument(this);
            });
  }

  public void removeTag(TagEntity tag) {
    Optional.ofNullable(tag)
        .ifPresent(
            t -> {
              Optional.ofNullable(this.tags).ifPresent(tagList -> tagList.remove(t));
              t.setDocument(null);
            });
  }
}
