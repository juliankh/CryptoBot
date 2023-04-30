-- Table: cb.config_queue_monitor

-- DROP TABLE IF EXISTS cb.config_queue_monitor;

CREATE TABLE IF NOT EXISTS cb.config_queue_monitor
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    queue_name varchar(256) NOT NULL,
    message_limit integer NOT NULL,
    CONSTRAINT config_queue_monitor_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.config_queue_monitor OWNER to postgres;

GRANT ALL ON TABLE cb.config_queue_monitor TO cryptobot;
GRANT SELECT ON TABLE cb.config_queue_monitor TO cryptobot_ro;
GRANT ALL ON TABLE cb.config_queue_monitor TO postgres;