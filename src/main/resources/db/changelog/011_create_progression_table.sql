-- liquibase formatted sql
-- changeset ines:011_create_progression_table

ALTER TABLE Resource
    DROP COLUMN resourceIsUsed;

 CREATE TABLE Progression (
     progressionId CHAR(36) NOT NULL DEFAULT (UUID()),
     exploited BOOLEAN NOT NULL DEFAULT FALSE,
     favorite BOOLEAN NOT NULL DEFAULT FALSE,
     setAside BOOLEAN NOT NULL DEFAULT FALSE,
     appUserId CHAR(36) NOT NULL,
     resourceId CHAR(36) NOT NULL,
     PRIMARY KEY (progressionId),
     UNIQUE KEY uq_progression_appUser_resource (appUserId, resourceId),
     CONSTRAINT fk_progression_appUser
         FOREIGN KEY (appUserId)
             REFERENCES AppUser(appUserId)
             ON DELETE CASCADE
             ON UPDATE CASCADE,
     CONSTRAINT fk_progression_resource
              FOREIGN KEY (resourceId)
                  REFERENCES Resource(resourceId)
                  ON DELETE CASCADE
                  ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
