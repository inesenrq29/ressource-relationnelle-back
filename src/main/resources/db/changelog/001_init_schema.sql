-- liquibase formatted sql
-- changeset sarah:001_init_schema

CREATE TABLE AppUser (
    appUserId CHAR(36) NOT NULL DEFAULT (UUID()),
    mail VARCHAR(255) NOT NULL,
    pseudo VARCHAR(100) NOT NULL,
    appUserIsActive BOOLEAN NOT NULL DEFAULT TRUE,
    hashedPassword VARCHAR(255) NOT NULL,
    previousPassword VARCHAR(255),
    lastConnectionAt TIMESTAMP NULL,
    PRIMARY KEY (appUserId),
    UNIQUE KEY uq_appUser_mail (mail),
    UNIQUE KEY uq_appUser_pseudo (pseudo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Message (
    messageId CHAR(36) NOT NULL DEFAULT (UUID()),
    titleType VARCHAR(100) NOT NULL,
    messageDescription TEXT,
    messageCreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    messageUpdatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    appUserId CHAR(36) NOT NULL,
    PRIMARY KEY (messageId),
    UNIQUE KEY uq_message_titleType (titleType),
    CONSTRAINT fk_message_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Friend (
  friendId CHAR(36) NOT NULL DEFAULT (UUID()),
  requesterUserId CHAR(36) NOT NULL,
  receiverUserId CHAR(36) NOT NULL,
  status ENUM('PENDING','ACCEPTED','REFUSED') NOT NULL DEFAULT 'PENDING',
  createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (friendId),

  UNIQUE KEY uq_friend_pair (requesterUserId, receiverUserId),

  CONSTRAINT fk_friend_requester
    FOREIGN KEY (requesterUserId)
      REFERENCES AppUser(appUserId)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

  CONSTRAINT fk_friend_receiver
    FOREIGN KEY (receiverUserId)
      REFERENCES AppUser(appUserId)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Resource (
    resourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    resourceIsActive BOOLEAN NOT NULL DEFAULT TRUE,
    resourceIsUsed BOOLEAN NOT NULL,
    resourceTitle VARCHAR(255) NOT NULL,
    resourceDescription TEXT,
    status ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL,
    resourceCreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resourceId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Comments (
    commentsId CHAR(36) NOT NULL DEFAULT (UUID()),
    publicationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationCommentsDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    author VARCHAR(100) NOT NULL,
    titleComments VARCHAR(100),
    commentsContent TEXT NOT NULL,
    resourceId CHAR(36) NOT NULL,
    PRIMARY KEY (commentsId),
    CONSTRAINT fk_comments_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE HistoryStateResource (
    historyStatusResourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    modificationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    oldResource VARCHAR(100) NOT NULL,
    newResource VARCHAR(100) NOT NULL,
    comment VARCHAR(255),
    resourceId CHAR(36) NOT NULL,
    PRIMARY KEY (historyStatusResourceId),
    CONSTRAINT fk_historyStateResource_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Favorite (
    favoriteId CHAR(36) NOT NULL DEFAULT (UUID()),
    appUserId CHAR(36) NOT NULL,
    resourceId CHAR(36) NOT NULL,
    PRIMARY KEY (favoriteId),
    UNIQUE KEY uq_favorite_user_resource (appUserId, resourceId)
    CONSTRAINT fk_favorite_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_favorite_resource
        FOREIGN KEY (resourceId)
            REFERENCES Resource(resourceId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Tag (
    tagId CHAR(36) NOT NULL DEFAULT (UUID()),
    wording VARCHAR(140) NOT NULL,
    PRIMARY KEY (tagId),
    UNIQUE KEY uq_tag_wording (wording),
    CONSTRAINT ck_tag_wording CHECK (CHAR_LENGTH(wording) >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE PDF (
    pdfId CHAR(36) NOT NULL DEFAULT (UUID()),
    pdfWeight VARCHAR(50) NOT NULL,
    PRIMARY KEY (pdfId),
    UNIQUE KEY uq_pdf_weight (pdfWeight)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Article (
    articleId CHAR(36) NOT NULL DEFAULT (UUID()),
    link VARCHAR(255) NOT NULL,
    PRIMARY KEY (articleId),
    UNIQUE KEY uq_article_link (link)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Video (
    videoId CHAR(36) NOT NULL DEFAULT (UUID()),
    videoWeight VARCHAR(50) NOT NULL,
    PRIMARY KEY (videoId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Game (
    gameId CHAR(36) NOT NULL DEFAULT (UUID()),
    gameWeight VARCHAR(50) NOT NULL,
    PRIMARY KEY (gameId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE LoginAttempt (
    loginAttemptId CHAR(36) NOT NULL DEFAULT (UUID()),
    appUserId CHAR(36),
    attemptedEmail VARCHAR(255) NOT NULL,
    attemptAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    ipAddress VARCHAR(45),
    PRIMARY KEY (loginAttemptId),
    CONSTRAINT fk_loginAttempt_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE SET NULL
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE PasswordResetToken (
    tokenId CHAR(36) NOT NULL DEFAULT (UUID()),
    appUserId CHAR(36) NOT NULL,
    tokenValue VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    expiresAt TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tokenId),
    UNIQUE KEY uq_passwordResetToken_value (tokenValue),
    CONSTRAINT fk_passwordResetToken_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT ck_passwordResetToken_type
        CHECK (type IN ('RESET_PASSWORD','EMAIL_VERIFICATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE RefreshToken (
    refreshTokenId CHAR(36) NOT NULL DEFAULT (UUID()),
    appUserId CHAR(36) NOT NULL,
    hashedToken VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiresAt TIMESTAMP NOT NULL,
    ipAddress VARCHAR(45),
    userAgent TEXT,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (refreshTokenId),
    UNIQUE KEY uq_refreshToken_hashedToken (hashedToken),
    CONSTRAINT fk_refreshToken_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;