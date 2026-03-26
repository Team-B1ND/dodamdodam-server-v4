CREATE TABLE oauth_clients(
 id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
 client_id VARCHAR(64) NOT NULL UNIQUE,
 client_secret VARCHAR(256) NOT NULL,
 owner_public_id VARCHAR(64) NOT NULL,
 client_name VARCHAR(100) NOT NULL,
 redirect_uris TEXT NOT NULL,
 scopes TEXT NOT NULL,
 website_url VARCHAR(512) NULL,
 description TEXT NULL,
 logo_url VARCHAR(512) NULL,
 is_active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE oauth_authorization_codes(
 id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
 code VARCHAR(128) NOT NULL UNIQUE,
 client_id VARCHAR(64) NOT NULL,
 user_public_id VARCHAR(64) NOT NULL,
 redirect_uri VARCHAR(512) NOT NULL,
 scopes TEXT NOT NULL,
 code_challenge VARCHAR(128) NULL,
 code_challenge_method VARCHAR(10) NULL,
 expires_at DATETIME NOT NULL,
 used BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE oauth_authorization_codes
 ADD INDEX idx_code (code);
ALTER TABLE oauth_authorization_codes
 ADD INDEX idx_expires (expires_at);

CREATE TABLE oauth_tokens(
 id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
 access_token_hash VARCHAR(64) NOT NULL UNIQUE,
 refresh_token_hash VARCHAR(64) NOT NULL UNIQUE,
 client_id VARCHAR(64) NOT NULL,
 user_public_id VARCHAR(64) NOT NULL,
 scopes TEXT NOT NULL,
 access_expires_at DATETIME NOT NULL,
 refresh_expires_at DATETIME NOT NULL,
 revoked BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE oauth_tokens
 ADD INDEX idx_user_client (user_public_id, client_id);

CREATE TABLE oauth_consents(
 id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
 user_public_id VARCHAR(64) NOT NULL,
 client_id VARCHAR(64) NOT NULL,
 scopes TEXT NOT NULL,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_user_client (user_public_id, client_id)
);

CREATE TABLE oauth_scopes(
 id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
 scope_key VARCHAR(64) NOT NULL UNIQUE,
 description VARCHAR(256) NOT NULL,
 is_active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO oauth_scopes (scope_key, description) VALUES
 ('nightstudy:read', '야간 자율학습 정보 조회'),
 ('nightstudy:write', '야간 자율학습 신청/취소'),
 ('outgoing:read', '외출/외박 정보 조회'),
 ('wakeupsong:read', '기상송 정보 조회'),
 ('wakeupsong:write', '기상송 신청'),
 ('profile:read', '기본 프로필 조회 (이름, 학번)'),
 ('notification:write', '푸시 알림 발송');
