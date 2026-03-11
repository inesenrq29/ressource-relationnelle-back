-- liquibase formatted sql
-- changeset ines:006_update_resource_with_category

ALTER TABLE Resource
    ADD COLUMN categoryId CHAR(36) NULL;

ALTER TABLE Resource
    ADD CONSTRAINT fk_resource_category
        FOREIGN KEY (categoryId)
            REFERENCES Category(categoryId)
            ON DELETE SET NULL
            ON UPDATE CASCADE;