CREATE TABLE project_rooms (
	id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
	created_at DATETIME NOT NULL,
	modified_at DATETIME NOT NULL,
	name VARCHAR(100) NOT NULL UNIQUE,
	floor INT NOT NULL
);

CREATE TABLE project_room_blocks (
	id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
	created_at DATETIME NOT NULL,
	modified_at DATETIME NOT NULL,
	fk_room_id BIGINT NOT NULL,
	start_at DATE NOT NULL,
	end_at DATE NOT NULL,
	period INT NOT NULL,
	description VARCHAR(250) NOT NULL,
	CONSTRAINT UK_PROJECT_ROOM_BLOCKS_ON_ROOM_PERIOD_RANGE UNIQUE (fk_room_id, period, start_at, end_at)
);

ALTER TABLE project_room_blocks
	ADD CONSTRAINT FK_PROJECT_ROOM_BLOCKS_ON_ROOM
	FOREIGN KEY (fk_room_id) REFERENCES project_rooms (id);

CREATE TABLE weekend_studies (
	id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
	created_at DATETIME NOT NULL,
	modified_at DATETIME NOT NULL,
	public_id BINARY(16) NOT NULL UNIQUE,
	fk_user_id BINARY(16) NOT NULL,
	date DATE NOT NULL,
	type VARCHAR(20) NOT NULL,
	status VARCHAR(10) NOT NULL,
	name VARCHAR(100) NULL,
	description VARCHAR(250) NULL,
	rejection_reason VARCHAR(255) NULL,
	fk_wish_room_id BIGINT NULL,
	fk_room_id BIGINT NULL
);

ALTER TABLE weekend_studies
	ADD CONSTRAINT FK_WEEKEND_STUDIES_ON_FK_WISH_ROOM
	FOREIGN KEY (fk_wish_room_id) REFERENCES project_rooms (id);

ALTER TABLE weekend_studies
	ADD CONSTRAINT FK_WEEKEND_STUDIES_ON_FK_ROOM
	FOREIGN KEY (fk_room_id) REFERENCES project_rooms (id);

CREATE TABLE weekend_study_periods (
	id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
	fk_weekend_study_id BIGINT NOT NULL,
	fk_user_id BINARY(16) NOT NULL,
	date DATE NOT NULL,
	period INT NOT NULL,
	CONSTRAINT UK_WEEKEND_STUDY_PERIODS_ON_USER_DATE_PERIOD UNIQUE (fk_user_id, date, period)
);

ALTER TABLE weekend_study_periods
	ADD CONSTRAINT FK_WEEKEND_STUDY_PERIODS_ON_WEEKEND_STUDY
	FOREIGN KEY (fk_weekend_study_id) REFERENCES weekend_studies (id);

CREATE TABLE weekend_study_attendances (
	id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
	created_at DATETIME NOT NULL,
	modified_at DATETIME NOT NULL,
	fk_weekend_study_period_id BIGINT NOT NULL UNIQUE,
	status VARCHAR(10) NOT NULL,
	fk_checker_id BINARY(16) NOT NULL
);

ALTER TABLE weekend_study_attendances
	ADD CONSTRAINT FK_WEEKEND_STUDY_ATTENDANCES_ON_PERIOD
	FOREIGN KEY (fk_weekend_study_period_id) REFERENCES weekend_study_periods (id);

CREATE TABLE weekend_study_deadlines (
	id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
	created_at DATETIME NOT NULL,
	modified_at DATETIME NOT NULL,
	start_day_of_week VARCHAR(10) NOT NULL,
	start_time TIME NOT NULL,
	end_day_of_week VARCHAR(10) NOT NULL,
	end_time TIME NOT NULL
);

INSERT INTO weekend_study_deadlines (created_at, modified_at, start_day_of_week, start_time, end_day_of_week, end_time)
VALUES (NOW(), NOW(), 'MONDAY', '00:00:00', 'SUNDAY', '17:00:00');
