CREATE TABLE night_study_deadlines (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at DATETIME NOT NULL,
    modified_at DATETIME NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

INSERT INTO night_study_deadlines (created_at, modified_at, start_time, end_time)
VALUES (NOW(), NOW(), '00:00:00', '20:30:00');
