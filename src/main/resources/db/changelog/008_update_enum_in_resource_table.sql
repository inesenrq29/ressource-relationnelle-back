-- liquibase formatted sql
-- changeset ines:008_update_enum_in_resource_table

ALTER TABLE Resource
    MODIFY COLUMN status ENUM(
    'DRAFT',
    'PENDING_VALIDATION',
    'PUBLISHED',
    'ARCHIVED',
    'RESTRICTED'
    ) NOT NULL;