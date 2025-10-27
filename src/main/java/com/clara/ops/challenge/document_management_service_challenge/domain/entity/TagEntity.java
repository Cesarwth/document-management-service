package com.clara.ops.challenge.document_management_service_challenge.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity
@Table(
    name = "tags",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"document_id", "tag_name"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_id", nullable = false)
  private DocumentEntity document;

  @Column(name = "tag_name", nullable = false)
  private String tagName;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TagEntity)) return false;
    TagEntity tag = (TagEntity) o;
    return Optional.ofNullable(this.tagName)
        .flatMap(tn -> Optional.ofNullable(tag.getTagName()).map(tn::equals))
        .orElse(false);
  }

  @Override
  public int hashCode() {
    return Optional.ofNullable(tagName).map(String::hashCode).orElse(getClass().hashCode());
  }
}
