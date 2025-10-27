-- H2 Test Database Schema
-- Tables will be created in document_schema as specified in application-test.yml

CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_name VARCHAR(255) NOT NULL,
    document_name VARCHAR(500) NOT NULL,
    minio_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id UUID NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_documents_user_name ON documents(user_name);
CREATE INDEX idx_documents_document_name ON documents(document_name);
CREATE INDEX idx_documents_created_at ON documents(created_at DESC);
CREATE INDEX idx_tags_tag_name ON tags(tag_name);
CREATE INDEX idx_tags_document_id ON tags(document_id);
CREATE UNIQUE INDEX idx_tags_unique_document_tag ON tags(document_id, tag_name);

