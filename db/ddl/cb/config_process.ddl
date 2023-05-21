-- Table: cb.config_process

-- DROP TABLE IF EXISTS cb.config_process;

CREATE TABLE IF NOT EXISTS cb.config_process
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    process_token varchar(256) NOT NULL,
    process_subtoken varchar(256),
    active boolean NOT NULL,
    CONSTRAINT config_process_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.config_process OWNER to postgres;

GRANT ALL ON TABLE cb.config_process TO cryptobot;
GRANT SELECT ON TABLE cb.config_process TO cryptobot_ro;
GRANT ALL ON TABLE cb.config_process TO postgres;