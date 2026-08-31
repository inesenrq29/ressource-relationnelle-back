-- liquibase formatted sql
-- changeset ines:002_fix_favorite_and_link_resources

ALTER TABLE Favorite
DROP FOREIGN KEY fk_favorite_resource;

ALTER TABLE Favorite
    CHANGE COLUMN ressourceId resourceId CHAR(36) NOT NULL;

ALTER TABLE Favorite
    ADD CONSTRAINT fk_favorite_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

ALTER TABLE Favorite
    ADD CONSTRAINT uq_favorite_user_resource
        UNIQUE (appUserId, resourceId);

ALTER TABLE PDF
    ADD COLUMN resourceId CHAR(36) NULL AFTER pdfId;

ALTER TABLE PDF
    MODIFY COLUMN pdfWeight BIGINT NOT NULL;

ALTER TABLE PDF
    ADD CONSTRAINT uq_pdf_resource UNIQUE (resourceId);

ALTER TABLE PDF
    ADD CONSTRAINT fk_pdf_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

ALTER TABLE Article
    ADD COLUMN resourceId CHAR(36) NULL AFTER articleId;

ALTER TABLE Article
    ADD CONSTRAINT uq_article_resource UNIQUE (resourceId);

ALTER TABLE Article
    ADD CONSTRAINT fk_article_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

ALTER TABLE Video
    ADD COLUMN resourceId CHAR(36) NULL AFTER videoId;

ALTER TABLE Video
    MODIFY COLUMN videoWeight BIGINT NOT NULL;

ALTER TABLE Video
    ADD CONSTRAINT uq_video_resource UNIQUE (resourceId);

ALTER TABLE Video
    ADD CONSTRAINT fk_video_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

ALTER TABLE Game
    ADD COLUMN resourceId CHAR(36) NULL AFTER gameId;

ALTER TABLE Game
    MODIFY COLUMN gameWeight BIGINT NOT NULL;

ALTER TABLE Game
    ADD CONSTRAINT uq_game_resource UNIQUE (resourceId);

ALTER TABLE Game
    ADD CONSTRAINT fk_game_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

ALTER TABLE PasswordResetToken
    MODIFY COLUMN type ENUM('RESET_PASSWORD','EMAIL_VERIFICATION') NOT NULL;
