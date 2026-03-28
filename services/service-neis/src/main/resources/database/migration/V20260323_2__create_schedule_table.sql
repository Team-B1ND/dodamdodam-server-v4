CREATE TABLE schedules(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id BINARY(16) NOT NULL,
    created_at datetime NOT NULL,
    modified_at datetime NOT NULL,
    title VARCHAR(100) NOT NULL,
    start_at date NOT NULL,
    end_at date NOT NULL,
    CONSTRAINT uk_schedule_public_id UNIQUE (public_id)
);

CREATE INDEX idx_schedule_start_at ON schedules (start_at);
CREATE INDEX idx_schedule_end_at ON schedules (end_at);

CREATE TABLE schedule_targets(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fk_schedule_id BIGINT NOT NULL,
    target VARCHAR(20) NOT NULL,
    CONSTRAINT fk_schedule_target_schedule FOREIGN KEY (fk_schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

CREATE INDEX idx_schedule_target_schedule_id ON schedule_targets (fk_schedule_id);
