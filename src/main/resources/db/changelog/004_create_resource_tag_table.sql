-- liquibase formatted sql
-- changeset ines:004_create_resource_tag_table

CREATE TABLE Resource_Tag (
    resourceId CHAR(36) NOT NULL,
    tagId CHAR(36) NOT NULL,
    PRIMARY KEY (resourceId, tagId),
    CONSTRAINT fk_resource_tag_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_resource_tag_tag
        FOREIGN KEY (tagId)
            REFERENCES Tag(tagId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
