-- liquibase formatted sql
-- changeset sarah:016_add_creator_to_resource

ALTER TABLE Resource
  ADD COLUMN creatorId CHAR(36) NULL,
  ADD CONSTRAINT fk_resource_creator
    FOREIGN KEY (creatorId)
      REFERENCES AppUser(appUserId)
      ON DELETE SET NULL
      ON UPDATE CASCADE;