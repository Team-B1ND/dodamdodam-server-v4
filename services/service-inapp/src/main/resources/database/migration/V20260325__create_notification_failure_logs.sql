CREATE TABLE notification_failure_logs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_public_id       VARCHAR(64)  NULL,
    app_name            VARCHAR(100) NULL,
    title               VARCHAR(256) NOT NULL,
    body                TEXT         NOT NULL,
    target_user_ids     TEXT         NULL,
    error_message       TEXT         NOT NULL,
    retry_count         INT          NOT NULL,
    occurred_at         DATETIME     NOT NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created (created_at DESC)
);

DROP TABLE IF EXISTS app_api_keys;
DROP TABLE IF EXISTS app_servers;
