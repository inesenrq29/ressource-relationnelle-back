-- liquibase formatted sql
-- changeset ines:014_update_quiz_table

ALTER TABLE QuizQuestion
ADD COLUMN correctAnswer BOOLEAN NOT NULL DEFAULT FALSE;

DROP TABLE IF EXISTS QuizOption;