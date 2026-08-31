-- liquibase formatted sql
-- changeset ines:013_create_many_resource

CREATE TABLE IF NOT EXISTS TextResource (
    textResourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    resourceId CHAR(36) NOT NULL,
    PRIMARY KEY (textResourceId),
    UNIQUE KEY uk_textresource_resourceId (resourceId),
    CONSTRAINT fk_textresource_resource
        FOREIGN KEY (resourceId)
        REFERENCES Resource(resourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS MediaResource (
    mediaResourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    mediaWeight BIGINT NOT NULL,
    resourceId CHAR(36) NOT NULL,
    PRIMARY KEY (mediaResourceId),
    UNIQUE KEY uk_mediaresource_resourceId (resourceId),
    CONSTRAINT fk_mediaresource_resource
        FOREIGN KEY (resourceId)
        REFERENCES Resource(resourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS InteractiveResource (
    interactiveResourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    activityType VARCHAR(20) NOT NULL,
    resourceId CHAR(36) NOT NULL,
    PRIMARY KEY (interactiveResourceId),
    UNIQUE KEY uk_interactiveresource_resourceId (resourceId),
    CONSTRAINT fk_interactiveresource_resource
        FOREIGN KEY (resourceId)
        REFERENCES Resource(resourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_interactiveresource_activityType
        CHECK (activityType IN ('QUIZ', 'POLL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Quiz (
    quizId CHAR(36) NOT NULL DEFAULT (UUID()),
    createdAt DATETIME(6) NOT NULL,
    interactiveResourceId CHAR(36) NOT NULL,
    PRIMARY KEY (quizId),
    UNIQUE KEY uk_quiz_interactiveResourceId (interactiveResourceId),
    CONSTRAINT fk_quiz_interactiveresource
        FOREIGN KEY (interactiveResourceId)
        REFERENCES InteractiveResource(interactiveResourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QuizQuestion (
    quizQuestionId CHAR(36) NOT NULL DEFAULT (UUID()),
    question TEXT NOT NULL,
    quizId CHAR(36) NOT NULL,
    PRIMARY KEY (quizQuestionId),
    KEY idx_quizquestion_quizId (quizId),
    CONSTRAINT fk_quizquestion_quiz
        FOREIGN KEY (quizId)
        REFERENCES Quiz(quizId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QuizOption (
    quizOptionId CHAR(36) NOT NULL DEFAULT (UUID()),
    optionLabel VARCHAR(255) NOT NULL,
    isCorrect BOOLEAN NOT NULL,
    quizQuestionId CHAR(36) NOT NULL,
    PRIMARY KEY (quizOptionId),
    KEY idx_quizoption_quizQuestionId (quizQuestionId),
    CONSTRAINT fk_quizoption_quizquestion
        FOREIGN KEY (quizQuestionId)
        REFERENCES QuizQuestion(quizQuestionId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Poll (
    pollId CHAR(36) NOT NULL DEFAULT (UUID()),
    createdAt DATETIME(6) NOT NULL,
    question TEXT NOT NULL,
    interactiveResourceId CHAR(36) NOT NULL,
    PRIMARY KEY (pollId),
    UNIQUE KEY uk_poll_interactiveResourceId (interactiveResourceId),
    CONSTRAINT fk_poll_interactiveresource
        FOREIGN KEY (interactiveResourceId)
        REFERENCES InteractiveResource(interactiveResourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS PollOption (
    pollOptionId CHAR(36) NOT NULL DEFAULT (UUID()),
    optionLabel VARCHAR(255) NOT NULL,
    pollId CHAR(36) NOT NULL,
    PRIMARY KEY (pollOptionId),
    KEY idx_polloption_pollId (pollId),
    CONSTRAINT fk_polloption_poll
        FOREIGN KEY (pollId)
        REFERENCES Poll(pollId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ActivitySession (
    activitySessionId CHAR(36) NOT NULL DEFAULT (UUID()),
    interactiveResourceId CHAR(36) NOT NULL,
    appUserId CHAR(36) NOT NULL,
    startedAt DATETIME(6) NOT NULL,
    PRIMARY KEY (activitySessionId),
    KEY idx_activitysession_interactiveResourceId (interactiveResourceId),
    KEY idx_activitysession_appUserId (appUserId),
    CONSTRAINT fk_activitysession_interactiveresource
        FOREIGN KEY (interactiveResourceId)
        REFERENCES InteractiveResource(interactiveResourceId)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_activitysession_appuser
        FOREIGN KEY (appUserId)
        REFERENCES AppUser(appUserId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE Resource
ADD CONSTRAINT chk_resource_resourceType
CHECK (resourceType IN (
    'CHALLENGE_CARD',
    'EXERCISE_WORKSHOP',
    'READING_SHEET',
    'ACTIVITY_GAME',
    'PDF',
    'ARTICLE',
    'GAME',
    'VIDEO'
));