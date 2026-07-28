CREATE TABLE night_study_team (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    public_id BINARY(16) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE night_study_team_member (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fk_project_team_id BIGINT NOT NULL,
    fk_user_id BINARY(16) NOT NULL,
    is_owner BOOLEAN NOT NULL DEFAULT FALSE,
    is_accept BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (fk_project_team_id) REFERENCES night_study_team(id) ON DELETE CASCADE
);

CREATE INDEX idx_night_study_team_member_fk_project_team_id ON night_study_team_member(fk_project_team_id);
CREATE INDEX idx_night_study_team_member_fk_user_id ON night_study_team_member(fk_user_id);