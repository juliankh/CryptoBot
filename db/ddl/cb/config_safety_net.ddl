-- Table: cb.config_safety_net

-- DROP TABLE IF EXISTS cb.config_safety_net;

CREATE TABLE IF NOT EXISTS cb.config_safety_net
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    process_token varchar(256) NOT NULL,
    process_subtoken varchar(256),
    active boolean NOT NULL,
    CONSTRAINT config_safety_net_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.config_safety_net OWNER to postgres;

GRANT ALL ON TABLE cb.config_safety_net TO cryptobot;
GRANT SELECT ON TABLE cb.config_safety_net TO cryptobot_ro;
GRANT ALL ON TABLE cb.config_safety_net TO postgres;