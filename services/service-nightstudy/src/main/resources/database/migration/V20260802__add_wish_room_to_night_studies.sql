ALTER TABLE night_studies
    ADD COLUMN fk_wish_room_id BIGINT NULL;

ALTER TABLE night_studies
    ADD CONSTRAINT FK_NIGHT_STUDIES_ON_FK_WISH_ROOM FOREIGN KEY (fk_wish_room_id) REFERENCES project_rooms (id);