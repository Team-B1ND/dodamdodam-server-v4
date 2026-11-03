DELETE m
FROM night_study_members m
JOIN night_studies n ON m.fk_night_study_id = n.id
WHERE n.type = 'AUTO';

DELETE FROM night_studies WHERE type = 'AUTO';

ALTER TABLE night_studies
    DROP FOREIGN KEY FK_NIGHT_STUDIES_ON_FK_SOURCE_PROJECT;

DROP INDEX IDX_NIGHT_STUDIES_SOURCE_PROJECT ON night_studies;

ALTER TABLE night_studies
    DROP COLUMN fk_source_project_id;
