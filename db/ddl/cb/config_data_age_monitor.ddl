-- Table: cb.config_data_age_monitor

-- DROP TABLE IF EXISTS cb.config_data_age_monitor;

CREATE TABLE IF NOT EXISTS cb.config_data_age_monitor
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    table_name varchar(256) NOT NULL,
    column_name varchar(256) NOT NULL,
    mins_age_limit integer NOT NULL,
    CONSTRAINT config_data_age_monitor_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.config_data_age_monitor OWNER to postgres;

GRANT ALL ON TABLE cb.config_data_age_monitor TO cryptobot;
GRANT SELECT ON TABLE cb.config_data_age_monitor TO cryptobot_ro;
GRANT ALL ON TABLE cb.config_data_age_monitor TO postgres;


