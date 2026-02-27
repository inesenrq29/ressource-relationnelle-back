--liquibase formatted sql
--changeset sarah:002_fix_resource_naming

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
    ADD UNIQUE KEY uq_favorite_user_resource (requesterUserId, resourceId);

ALTER TABLE Friend
    ADD COLUMN requesterUserId CHAR(36) NOT NULL,
    ADD COLUMN friendReceiverUserId CHAR(36) NOT NULL;

ALTER TABLE Friend
    ADD CONSTRAINT fk_friend_requester
        FOREIGN KEY (requesterUserId)
        REFERENCES AppUser(appUserId)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

ALTER TABLE Friend
    ADD CONSTRAINT fk_friend_receiver
        FOREIGN KEY (friendReceiverUserId)
        REFERENCES AppUser(appUserId)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

ALTER TABLE Friend
    ADD UNIQUE KEY uq_friend_pair (requesterUserId, friendReceiverUserId);

ALTER TABLE Game
    ADD COLUMN resourceId CHAR(36) NOT NULL,
    ADD CONSTRAINT fk_game_resource
        FOREIGN KEY (resourceId)
        REFERENCES Resource(resourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

ALTER TABLE Video
    ADD COLUMN resourceId CHAR(36) NOT NULL,
    ADD CONSTRAINT fk_video_resource
        FOREIGN KEY (resourceId)
        REFERENCES Resource(resourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

ALTER TABLE Article
    ADD COLUMN resourceId CHAR(36) NOT NULL,
    ADD CONSTRAINT fk_article_resource
        FOREIGN KEY (resourceId)
        REFERENCES Resource(resourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

ALTER TABLE PDF
    ADD COLUMN resourceId CHAR(36) NOT NULL,
    ADD CONSTRAINT fk_pdf_resource
        FOREIGN KEY (resourceId)
        REFERENCES Resource(resourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE;
