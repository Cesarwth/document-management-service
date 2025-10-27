package com.clara.ops.challenge.document_management_service_challenge.domain.repository;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.TagEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class DocumentSpecification {

  public static Specification<DocumentEntity> withFilters(DocumentSearchFilters filters) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      DocumentSearchFilters safeFilters =
          Optional.ofNullable(filters).orElse(new DocumentSearchFilters());

      Optional.ofNullable(safeFilters.getUser())
          .filter(user -> !user.isBlank())
          .ifPresent(user -> predicates.add(criteriaBuilder.equal(root.get("userName"), user)));

      Optional.ofNullable(safeFilters.getName())
          .filter(name -> !name.isBlank())
          .ifPresent(
              name ->
                  predicates.add(
                      criteriaBuilder.like(
                          criteriaBuilder.lower(root.get("documentName")),
                          "%" + name.toLowerCase() + "%")));

      Optional.ofNullable(safeFilters.getTags())
          .filter(tags -> !tags.isEmpty())
          .ifPresent(
              tags ->
                  tags.stream()
                      .filter(tag -> tag != null && !tag.isBlank())
                      .forEach(
                          tag -> {
                            Subquery<Long> subquery = query.subquery(Long.class);
                            Root<TagEntity> tagRoot = subquery.from(TagEntity.class);
                            subquery.select(criteriaBuilder.count(tagRoot));
                            subquery.where(
                                criteriaBuilder.and(
                                    criteriaBuilder.equal(tagRoot.get("document"), root),
                                    criteriaBuilder.equal(tagRoot.get("tagName"), tag)));

                            predicates.add(criteriaBuilder.greaterThan(subquery, 0L));
                          }));

      query.distinct(true);

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
