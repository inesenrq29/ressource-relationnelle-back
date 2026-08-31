-- liquibase formatted sql
-- changeset ines:009_add_resource_type_enum

ALTER TABLE Resource ADD COLUMN resourceType VARCHAR(20);