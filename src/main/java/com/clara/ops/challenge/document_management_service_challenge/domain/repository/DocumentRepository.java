package com.clara.ops.challenge.document_management_service_challenge.domain.repository;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository
    extends JpaRepository<DocumentEntity, UUID>, JpaSpecificationExecutor<DocumentEntity> {}
