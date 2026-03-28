CREATE TABLE banners (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at datetime NOT NULL,
    modified_at datetime NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    link_url VARCHAR(512) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE
);
