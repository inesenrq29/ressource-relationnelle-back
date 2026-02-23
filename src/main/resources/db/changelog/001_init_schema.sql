-- liquibase formatted sql
-- changeset sarah:001_init_schema

CREATE TABLE user (
    userId CHAR(36) NOT NULL DEFAULT (UUID()),
    mail VARCHAR(255) NOT NULL,
    pseudo VARCHAR(100) NOT NULL,
    userIsActive BOOLEAN NOT NULL DEFAULT TRUE,
    hashedPassword VARCHAR(255) NOT NULL,
    previousPassword VARCHAR(255),
    lastConnectionAt TIMESTAMP NULL,
    PRIMARY KEY (userId),
    UNIQUE KEY uq_user_mail (mail),
    UNIQUE KEY uq_user_pseudo (pseudo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
    messageId CHAR(36) NOT NULL DEFAULT (UUID()),
    titleType VARCHAR(100) NOT NULL,
    messageDescription TEXT,
    messageCreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    messageUpdatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (messageId),
    UNIQUE KEY uq_message_titleType (titleType)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comment (
    commentId CHAR(36) NOT NULL DEFAULT (UUID()),
    publicationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationCommentDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    autor VARCHAR(100) NOT NULL,
    titleComment VARCHAR(100),
    commentContent TEXT NOT NULL,
    PRIMARY KEY (commentId),
    UNIQUE KEY uq_comment_id (commentId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE friends (
    friendId CHAR(36) NOT NULL DEFAULT (UUID()),
    PRIMARY KEY (friendId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE resources (
    resourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    ressourceIsActive BOOLEAN NOT NULL DEFAULT TRUE,
    ressourceIsUsed BOOLEAN NOT NULL,
    resourceTitle VARCHAR(255) NOT NULL,
    resourceDescription TEXT,
    status ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL,
    ressourceCreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resourceId),
    UNIQUE KEY uq_resource_id (resourceId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historyStateResource (
    historyStatusResourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    modificationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    oldResource VARCHAR(100) NOT NULL,
    newResource VARCHAR(100) NOT NULL,
    comment VARCHAR(255),
    PRIMARY KEY (historyStatusResourceId),
    UNIQUE KEY uq_history_id (historyStatusResourceId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE favorite (
    favoriteId CHAR(36) NOT NULL DEFAULT (UUID()),
    ressourceId CHAR(36) NOT NULL,
    PRIMARY KEY (favoriteId),
    UNIQUE KEY uq_favorite_id (favoriteId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tag (
    tagId CHAR(36) NOT NULL DEFAULT (UUID()),
    wording VARCHAR(140) NOT NULL,
    PRIMARY KEY (tagId),
    UNIQUE KEY uq_tag_wording (wording),
    CONSTRAINT ck_tag_wording CHECK (CHAR_LENGTH(wording) >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pdf (
    pdfWeight VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_pdf_weight (pdfWeight)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE article (
    link VARCHAR(255) NOT NULL,
    UNIQUE KEY uq_article_link (link)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE video (
    videoWeight VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE game (
    gameWeight VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loginAttempt (
    loginAttemptId CHAR(36) NOT NULL DEFAULT (UUID()),
    userId CHAR(36),
    attemptedEmail VARCHAR(255) NOT NULL,
    attemptAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    ipAddress VARCHAR(45),
    PRIMARY KEY (loginAttemptId),
    CONSTRAINT fk_loginAttempt_user
        FOREIGN KEY (userId)
            REFERENCES user(userId)
            ON DELETE SET NULL
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE passwordResetToken (
    tokenId CHAR(36) NOT NULL DEFAULT (UUID()),
    userId CHAR(36) NOT NULL,
    tokenValue VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    expiresAt TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tokenId),
    UNIQUE KEY uq_token_value (tokenValue),
    CONSTRAINT fk_passwordReset_user
        FOREIGN KEY (userId)
            REFERENCES user(userId)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT ck_token_type
        CHECK (type IN ('RESET_PASSWORD','EMAIL_VERIFICATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refreshToken (
    refreshTokenId CHAR(36) NOT NULL DEFAULT (UUID()),
    userId CHAR(36) NOT NULL,
    hashedToken VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiresAt TIMESTAMP NOT NULL,
    ipAddress VARCHAR(45),
    userAgent TEXT,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (refreshTokenId),
    UNIQUE KEY uq_refresh_hashed (hashedToken),
    CONSTRAINT fk_refresh_user
        FOREIGN KEY (userId)
            REFERENCES user(userId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;