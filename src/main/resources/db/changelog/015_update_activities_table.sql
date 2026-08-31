-- liquibase formatted sql
-- changeset ines:015_update_activities_table

ALTER TABLE ActivitySession
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' AFTER interactiveResourceId;

ALTER TABLE ActivitySession
ADD COLUMN createdById CHAR(36) NULL AFTER startedAt;

UPDATE ActivitySession
SET createdById = appUserId
WHERE createdById IS NULL;

ALTER TABLE ActivitySession
MODIFY COLUMN createdById CHAR(36) NOT NULL;

ALTER TABLE ActivitySession
ADD CONSTRAINT fk_activitysession_createdby
FOREIGN KEY (createdById) REFERENCES AppUser(appUserId)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE ActivitySession
ADD KEY idx_activitysession_createdById (createdById);

ALTER TABLE ActivitySession
DROP FOREIGN KEY fk_activitysession_appuser;

ALTER TABLE ActivitySession
DROP INDEX idx_activitysession_appUserId;

ALTER TABLE ActivitySession
DROP COLUMN appUserId;


CREATE TABLE ActivityParticipant (
    activityParticipantId CHAR(36) NOT NULL DEFAULT (uuid()),
    activitySessionId CHAR(36) NOT NULL,
    appUserId CHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    invitedAt DATETIME(6) NULL,
    joinedAt DATETIME(6) NULL,
    PRIMARY KEY (activityParticipantId),
    KEY idx_activityparticipant_activitySessionId (activitySessionId),
    KEY idx_activityparticipant_appUserId (appUserId),
    CONSTRAINT fk_activityparticipant_activitysession
        FOREIGN KEY (activitySessionId) REFERENCES ActivitySession(activitySessionId)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_activityparticipant_appuser
        FOREIGN KEY (appUserId) REFERENCES AppUser(appUserId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE ActivityMessage (
    activityMessageId CHAR(36) NOT NULL DEFAULT (uuid()),
    activitySessionId CHAR(36) NOT NULL,
    appUserId CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    sentAt DATETIME(6) NOT NULL,
    PRIMARY KEY (activityMessageId),
    KEY idx_activitymessage_activitySessionId (activitySessionId),
    KEY idx_activitymessage_appUserId (appUserId),
    CONSTRAINT fk_activitymessage_activitysession
        FOREIGN KEY (activitySessionId) REFERENCES ActivitySession(activitySessionId)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_activitymessage_appuser
        FOREIGN KEY (appUserId) REFERENCES AppUser(appUserId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE QuizParticipantAnswer (
    quizParticipantAnswerId CHAR(36) NOT NULL DEFAULT (uuid()),
    activityParticipantId CHAR(36) NOT NULL,
    quizQuestionId CHAR(36) NOT NULL,
    answeredAt DATETIME(6) NULL,
    userAnswer BIT(1) NOT NULL,
    PRIMARY KEY (quizParticipantAnswerId),
    KEY idx_quizparticipantanswer_activityParticipantId (activityParticipantId),
    KEY idx_quizparticipantanswer_quizQuestionId (quizQuestionId),
    CONSTRAINT fk_quizparticipantanswer_activityparticipant
        FOREIGN KEY (activityParticipantId) REFERENCES ActivityParticipant(activityParticipantId)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_quizparticipantanswer_quizquestion
        FOREIGN KEY (quizQuestionId) REFERENCES QuizQuestion(quizQuestionId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE PollParticipantAnswer (
    pollParticipantAnswerId CHAR(36) NOT NULL DEFAULT (uuid()),
    activityParticipantId CHAR(36) NOT NULL,
    pollOptionId CHAR(36) NOT NULL,
    answeredAt DATETIME(6) NULL,
    PRIMARY KEY (pollParticipantAnswerId),
    KEY idx_pollparticipantanswer_activityParticipantId (activityParticipantId),
    KEY idx_pollparticipantanswer_pollOptionId (pollOptionId),
    CONSTRAINT fk_pollparticipantanswer_activityparticipant
        FOREIGN KEY (activityParticipantId) REFERENCES ActivityParticipant(activityParticipantId)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_pollparticipantanswer_polloption
        FOREIGN KEY (pollOptionId) REFERENCES PollOption(pollOptionId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;