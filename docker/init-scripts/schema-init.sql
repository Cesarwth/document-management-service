-- Document Management Service Schema
-- This schema handles document metadata and tags

CREATE SCHEMA IF NOT EXISTS document_schema;
SET SCHEMA 'document_schema';

-- Documents table to store document metadata
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_name VARCHAR(255) NOT NULL,
    document_name VARCHAR(500) NOT NULL,
    minio_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tags table to store tags associated with documents (many-to-many relationship)
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    document_id UUID NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

-- Indexes for optimizing search queries
-- Index on user_name for filtering by user
CREATE INDEX idx_documents_user_name ON documents(user_name);

-- Index on document_name for filtering by name
CREATE INDEX idx_documents_document_name ON documents(document_name);

-- Index on created_at for sorting (descending order is commonly used)
CREATE INDEX idx_documents_created_at ON documents(created_at DESC);

-- Composite index for common query patterns
CREATE INDEX idx_documents_user_created ON documents(user_name, created_at DESC);

-- Index on tags for filtering by tags
CREATE INDEX idx_tags_tag_name ON tags(tag_name);

-- Index on document_id in tags table for join operations
CREATE INDEX idx_tags_document_id ON tags(document_id);

-- Composite index on tags for efficient tag searching
CREATE INDEX idx_tags_document_tag ON tags(document_id, tag_name);

-- Optional: Add a unique constraint to prevent duplicate tags per document
CREATE UNIQUE INDEX idx_tags_unique_document_tag ON tags(document_id, tag_name);
