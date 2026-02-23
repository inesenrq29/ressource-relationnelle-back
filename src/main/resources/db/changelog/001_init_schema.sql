-- liquibase formatted sql
-- changeset sarah:001_init_schema

CREATE TABLE role (
  role_id CHAR(36) NOT NULL DEFAULT (UUID()),
  role_name VARCHAR(50) NOT NULL,
  PRIMARY KEY (role_id),
  UNIQUE KEY uq_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user (
    user_id CHAR(36) NOT NULL DEFAULT (UUID()),
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    role_id CHAR(36) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_connection_at TIMESTAMP NULL,
    are_terms_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    is_privacy_policy_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id),
    UNIQUE KEY uq_user_email (email),

    CONSTRAINT ck_user_status
        CHECK (status IN ('ACTIVE', 'DISABLED')),

    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id)
            REFERENCES role(role_id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE breathing_configuration (
    configuration_id CHAR(36) NOT NULL DEFAULT (UUID()),
    configuration_name VARCHAR(100) NOT NULL,
    inhale_time SMALLINT NOT NULL,
    hold_time SMALLINT NOT NULL,
    exhale_time SMALLINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    user_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cycles SMALLINT NOT NULL DEFAULT 5,

    PRIMARY KEY (configuration_id),

    CONSTRAINT ck_breathing_inhale_time CHECK (inhale_time > 0),
    CONSTRAINT ck_breathing_hold_time CHECK (hold_time >= 0),
    CONSTRAINT ck_breathing_exhale_time CHECK (exhale_time > 0),
    CONSTRAINT ck_breathing_cycles CHECK (cycles > 0),

    CONSTRAINT ck_breathing_type CHECK (type IN ('SYSTEM', 'USER')),

    CONSTRAINT ck_breathing_type_user
        CHECK (
            (type = 'SYSTEM' AND user_id IS NULL)
            OR
            (type = 'USER' AND user_id IS NOT NULL)
        ),

    CONSTRAINT fk_breathing_configuration_user
        FOREIGN KEY (user_id)
            REFERENCES user(user_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu (
    menu_id CHAR(36) NOT NULL DEFAULT (UUID()),
    title_menu VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    menu_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (menu_id),
    UNIQUE KEY uq_menu_slug (slug),

    CONSTRAINT ck_menu_status
        CHECK (menu_status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE page_content (
    page_id CHAR(36) NOT NULL DEFAULT (UUID()),
    title_page VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    page_status VARCHAR(20) NOT NULL,
    menu_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,

    PRIMARY KEY (page_id),
    UNIQUE KEY uq_page_slug (slug),

    CONSTRAINT ck_page_status
        CHECK (page_status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),

    CONSTRAINT fk_page_content_menu
        FOREIGN KEY (menu_id)
            REFERENCES menu(menu_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE login_attempt (
    login_attempt_id CHAR(36) NOT NULL DEFAULT (UUID()),
    user_id CHAR(36),
    attempted_email VARCHAR(255) NOT NULL,
    attempt_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(45),

    PRIMARY KEY (login_attempt_id),

    CONSTRAINT fk_login_attempt_user
        FOREIGN KEY (user_id)
            REFERENCES user(user_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_token (
    token_id CHAR(36) NOT NULL DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    token_value VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (token_id),
    UNIQUE KEY uq_password_reset_token_value (token_value),

    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id)
            REFERENCES user(user_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT ck_password_reset_token_type
        CHECK (type IN ('RESET_PASSWORD', 'EMAIL_VERIFICATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_token (
    refresh_token_id CHAR(36) NOT NULL DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    hashed_token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (refresh_token_id),
    UNIQUE KEY uq_refresh_token_hashed (hashed_token),

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES user(user_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE log_action (
    log_id CHAR(36) NOT NULL DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    description TEXT,
    action_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),

    PRIMARY KEY (log_id),

    CONSTRAINT fk_log_action_user
        FOREIGN KEY (user_id)
            REFERENCES user(user_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX index_user_role_id ON user(role_id);
CREATE INDEX index_breathing_configuration_user_id ON breathing_configuration(user_id);
CREATE INDEX index_page_content_menu_id ON page_content(menu_id);
CREATE INDEX index_login_attempt_user_id ON login_attempt(user_id);
CREATE INDEX index_password_reset_token_user_id ON password_reset_token(user_id);
CREATE INDEX index_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX index_log_action_user_id ON log_action(user_id);