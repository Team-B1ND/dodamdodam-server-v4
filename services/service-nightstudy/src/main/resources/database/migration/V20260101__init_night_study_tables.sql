CREATE TABLE project_rooms (
    id   BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name VARCHAR(100)          NOT NULL UNIQUE
);

CREATE TABLE night_studies (
    id                BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at        DATETIME              NOT NULL,
    modified_at       DATETIME              NOT NULL,
    public_id         BINARY(16)            NOT NULL UNIQUE,
    name              VARCHAR(100)          NULL,
    description       VARCHAR(250)          NOT NULL,
    period            INT                   NOT NULL,
    start_at          DATE                  NOT NULL,
    end_at            DATE                  NOT NULL,
    need_phone        BIT(1)                NOT NULL,
    need_phone_reason TEXT                  NULL,
    status            VARCHAR(10)           NOT NULL,
    rejection_reason  TEXT                  NULL,
    type              VARCHAR(10)           NOT NULL,
    fk_room_id        BIGINT                NULL
);

CREATE TABLE night_study_members (
    id               BIGINT     AUTO_INCREMENT NOT NULL PRIMARY KEY,
    fk_night_study_id BIGINT    NOT NULL,
    fk_user_id        BINARY(16) NOT NULL,
    is_leader         BIT(1)     NOT NULL
);

CREATE TABLE night_study_banned (
    id          BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at  DATETIME              NOT NULL,
    modified_at DATETIME              NOT NULL,
    fk_user_id  BINARY(16)            NOT NULL,
    reason      VARCHAR(255)          NOT NULL,
    end_at      DATE                  NOT NULL
);

ALTER TABLE night_studies
    ADD CONSTRAINT FK_NIGHT_STUDIES_ON_FK_PROJECT_ROOM
    FOREIGN KEY (fk_room_id) REFERENCES project_rooms (id);

ALTER TABLE night_study_members
    ADD CONSTRAINT FK_NIGHT_STUDY_MEMBERS_ON_FK_NIGHT_STUDY
    FOREIGN KEY (fk_night_study_id) REFERENCES night_studies (id);
