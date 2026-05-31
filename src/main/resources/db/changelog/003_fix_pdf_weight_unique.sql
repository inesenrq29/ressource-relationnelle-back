-- liquibase formatted sql
-- changeset ines:003_fix_pdf_weight_unique

ALTER TABLE PDF
DROP INDEX uq_pdf_weight;