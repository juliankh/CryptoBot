-- Table: cb.config_misc

-- DROP TABLE IF EXISTS cb.config_misc;

CREATE TABLE IF NOT EXISTS cb.config_misc
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    name varchar(256) NOT NULL,
    value double precision NOT NULL,
    CONSTRAINT config_misc_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.config_misc OWNER to postgres;

GRANT ALL ON TABLE cb.config_misc TO cryptobot;
GRANT SELECT ON TABLE cb.config_misc TO cryptobot_ro;
GRANT ALL ON TABLE cb.config_misc TO postgres;


