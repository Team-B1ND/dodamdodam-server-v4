ALTER TABLE weekend_studies
	DROP FOREIGN KEY FK_WEEKEND_STUDIES_ON_FK_WISH_ROOM;

ALTER TABLE weekend_studies
	DROP COLUMN fk_wish_room_id,
	DROP COLUMN status,
	DROP COLUMN rejection_reason;
