-- liquibase formatted sql
-- changeset ines:012_create_share_resource_table

 CREATE TABLE ShareResource (
     shareResourceId CHAR(36) NOT NULL DEFAULT (UUID()),
     message TEXT,
     sharedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     receiverUserId CHAR(36) NOT NULL,
     senderUserId CHAR(36) NOT NULL,
     resourceId CHAR(36) NOT NULL,
     PRIMARY KEY (shareResourceId),
     UNIQUE KEY uq_share_resource_receiver_sender_resource (receiverUserId, senderUserId, resourceId),
     CONSTRAINT fk_share_resource_receiver
         FOREIGN KEY (receiverUserId)
             REFERENCES AppUser(appUserId)
             ON DELETE CASCADE
             ON UPDATE CASCADE,
     CONSTRAINT fk_share_resource_sender
              FOREIGN KEY (senderUserId)
                  REFERENCES AppUser(appUserId)
                  ON DELETE CASCADE
                  ON UPDATE CASCADE,
     CONSTRAINT fk_share_resource_resource
              FOREIGN KEY (resourceId)
                  REFERENCES Resource(resourceId)
                  ON DELETE CASCADE
                  ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
