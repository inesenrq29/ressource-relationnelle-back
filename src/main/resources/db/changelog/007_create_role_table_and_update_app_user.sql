-- liquibase formatted sql
-- changeset ines:007_create_role_table_and_update_app_user

--Create table role
CREATE TABLE Role (
  roleId CHAR(36) NOT NULL DEFAULT (UUID()),
  roleName VARCHAR(50) NOT NULL,
  PRIMARY KEY (roleId),
  UNIQUE KEY uq_role_roleName (roleName)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert role into the role table
INSERT INTO Role (roleName)
VALUES
    ('USER'),
    ('MODERATOR'),
    ('ADMIN'),
    ('SUPER_ADMIN');

-- update AppUser table by adding new fields
ALTER TABLE AppUser
    ADD COLUMN roleId CHAR(36) NULL,
    ADD COLUMN status VARCHAR(20) NULL,
    ADD COLUMN areTermsAccepted BOOLEAN NULL,
    ADD COLUMN isPrivacyPolicyAccepted BOOLEAN NULL,
    ADD COLUMN createdAt TIMESTAMP NULL,
    ADD COLUMN updatedAt TIMESTAMP NULL;

-- update AppUser by adding by default USER role
UPDATE AppUser
SET roleId = (
    SELECT roleId
    FROM Role
    WHERE roleName = 'USER'
    LIMIT 1
    )
WHERE roleId IS NULL;

-- update AppUser by adding by default ACTIVE status
UPDATE AppUser
SET status = 'ACTIVE'
WHERE status IS NULL;

-- update AppUser by adding by default false to termsAccepted and privacy policy
UPDATE AppUser
SET areTermsAccepted = FALSE
WHERE areTermsAccepted IS NULL;

UPDATE AppUser
SET isPrivacyPolicyAccepted = FALSE
WHERE isPrivacyPolicyAccepted IS NULL;

-- update AppUser by adding by default current time for createdAt and updatedAt
UPDATE AppUser
SET createdAt = CURRENT_TIMESTAMP
WHERE createdAt IS NULL;

UPDATE AppUser
SET updatedAt = CURRENT_TIMESTAMP
WHERE updatedAt IS NULL;

-- update AppUser by adding constraint not null
ALTER TABLE AppUser
    MODIFY COLUMN roleId CHAR(36) NOT NULL,
    MODIFY COLUMN status VARCHAR(20) NOT NULL,
    MODIFY COLUMN areTermsAccepted BOOLEAN NOT NULL,
    MODIFY COLUMN isPrivacyPolicyAccepted BOOLEAN NOT NULL,
    MODIFY COLUMN createdAt TIMESTAMP NOT NULL;

-- add role as a FK
ALTER TABLE AppUser
    ADD CONSTRAINT fk_appUser_role
        FOREIGN KEY (roleId)
            REFERENCES Role(roleId)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;