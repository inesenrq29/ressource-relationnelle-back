-- liquibase formatted sql
-- changeset ines:010_update_comments_table

ALTER TABLE Comments
    ADD COLUMN parentCommentId CHAR(36),
    ADD COLUMN moderationReason VARCHAR(255),
    ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING';