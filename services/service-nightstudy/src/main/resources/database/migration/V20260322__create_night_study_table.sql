CREATE TABLE night_studies (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at DATETIME NOT NULL,
    modified_at DATETIME NOT NULL,
    name VARCHAR(100) NULL,
    description VARCHAR(250) NOT NULL,
    period INT NOT NULL,
    start_at DATE NOT NULL,
    end_at DATE NOT NULL,
    need_phone BOOLEAN NOT NULL,
    need_phone_reason VARCHAR(255) NULL,
    status VARCHAR(10) NOT NULL,
    fk_user_id BINARY(16) NOT NULL,
    rejection_reason VARCHAR(255) NULL,
    type VARCHAR(10) NOT NULL
);

CREATE TABLE night_study_members (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    fk_night_study_id BIGINT NOT NULL,
    fk_user_id BINARY(16) NOT NULL
);

CREATE TABLE night_study_banned (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at DATETIME NOT NULL,
    modified_at DATETIME NOT NULL,
    fk_user_id BINARY(16) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    end_at DATE NOT NULL
);
