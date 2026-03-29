CREATE TABLE out_sleepings(
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    public_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,
    fk_user_id BINARY(16) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    start_at DATE NOT NULL,
    end_at DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    deny_reason VARCHAR(255),
    CONSTRAINT uk_out_sleepings_public_id UNIQUE (public_id)
);

CREATE INDEX idx_out_sleepings_start_at_end_at ON out_sleepings(start_at, end_at);

CREATE TABLE out_sleeping_deadlines(
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,
    start_day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL DEFAULT '00:00:00',
    end_day_of_week VARCHAR(10) NOT NULL DEFAULT 'FRIDAY',
    end_time TIME NOT NULL DEFAULT '17:00:00'
);

INSERT INTO out_sleeping_deadlines (start_day_of_week, start_time, end_day_of_week, end_time, created_at, modified_at)
VALUES ('SUNDAY', '00:00:00', 'SUNDAY', '17:00:00', NOW(), NOW());