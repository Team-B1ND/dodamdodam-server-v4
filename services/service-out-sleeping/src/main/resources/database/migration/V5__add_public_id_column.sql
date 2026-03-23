ALTER TABLE out_sleepings
    ADD COLUMN public_id BINARY(16) NOT NULL AFTER id;

CREATE UNIQUE INDEX uk_out_sleepings_public_id ON out_sleepings (public_id);
