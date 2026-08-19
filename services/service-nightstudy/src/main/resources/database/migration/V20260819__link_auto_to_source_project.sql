ALTER TABLE night_studies
    ADD COLUMN fk_source_project_id BIGINT NULL;

CREATE INDEX IDX_NIGHT_STUDIES_SOURCE_PROJECT
    ON night_studies (fk_source_project_id);

ALTER TABLE night_studies
    ADD CONSTRAINT FK_NIGHT_STUDIES_ON_FK_SOURCE_PROJECT
    FOREIGN KEY (fk_source_project_id) REFERENCES night_studies (id);
