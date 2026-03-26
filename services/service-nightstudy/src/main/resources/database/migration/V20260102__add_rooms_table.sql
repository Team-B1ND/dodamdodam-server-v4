CREATE TABLE rooms (
    id          BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at  DATETIME     NOT NULL,
    modified_at DATETIME     NOT NULL,
    public_id   BINARY(16)   NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL UNIQUE
);
