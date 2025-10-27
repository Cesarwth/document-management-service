package com.clara.ops.challenge.document_management_service_challenge.mapper;

import com.clara.ops.challenge.document_management_service_challenge.api.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.TagEntity;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DocumentMapper {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  public DocumentDto toDto(DocumentEntity entity) {
    DocumentEntity validEntity =
        Optional.ofNullable(entity)
            .orElseThrow(
                () -> new InvalidDocumentException("Cannot map null entity to DocumentDto"));

    return DocumentDto.builder()
        .id(
            Optional.ofNullable(validEntity.getId())
                .map(Object::toString)
                .orElseThrow(() -> new InvalidDocumentException("Document ID cannot be null")))
        .user(
            Optional.ofNullable(validEntity.getUserName())
                .orElseThrow(() -> new InvalidDocumentException("User name cannot be null")))
        .name(
            Optional.ofNullable(validEntity.getDocumentName())
                .orElseThrow(() -> new InvalidDocumentException("Document name cannot be null")))
        .tags(
            Optional.ofNullable(validEntity.getTags())
                .map(
                    tagList ->
                        tagList.stream()
                            .map(TagEntity::getTagName)
                            .filter(tagName -> tagName != null && !tagName.isBlank())
                            .collect(Collectors.toList()))
                .orElse(Collections.emptyList()))
        .size(
            Optional.ofNullable(validEntity.getFileSize())
                .orElseThrow(() -> new InvalidDocumentException("File size cannot be null")))
        .type(
            Optional.ofNullable(validEntity.getFileType())
                .orElseThrow(() -> new InvalidDocumentException("File type cannot be null")))
        .createdAt(
            Optional.ofNullable(validEntity.getCreatedAt())
                .map(DATE_TIME_FORMATTER::format)
                .orElseThrow(() -> new InvalidDocumentException("Created date cannot be null")))
        .build();
  }
}
