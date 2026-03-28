RENAME TABLE schedules TO time_tables;

ALTER TABLE time_tables CHANGE COLUMN schedule_date time_table_date date NOT NULL;

ALTER TABLE time_tables DROP INDEX uk_schedule_date_grade_class_period;
ALTER TABLE time_tables ADD CONSTRAINT uk_time_table_date_grade_room_period UNIQUE (time_table_date, grade, room, period);

DROP INDEX idx_schedule_date ON time_tables;
DROP INDEX idx_schedule_date_grade_class ON time_tables;

CREATE INDEX idx_time_table_date ON time_tables (time_table_date);
CREATE INDEX idx_time_table_date_grade_room ON time_tables (time_table_date, grade, room);
