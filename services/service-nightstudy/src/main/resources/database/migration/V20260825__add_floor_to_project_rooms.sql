ALTER TABLE project_rooms
    ADD COLUMN floor INT NOT NULL DEFAULT 2;

UPDATE project_rooms
SET floor = 3
WHERE name = 'LAB13';
