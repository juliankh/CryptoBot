-- Table: cb.kraken_status_update

-- DROP TABLE IF EXISTS cb.kraken_status_update;

CREATE TABLE IF NOT EXISTS cb.kraken_status_update
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    channel varchar(256) NOT NULL,
    type varchar(256) NOT NULL,
    api_version varchar(256) NOT NULL,
    connection_id numeric,
    system varchar(256) NOT NULL,
    version varchar(256) NOT NULL,
    symbol varchar(256),
    created timestamp with time zone NOT NULL,
    CONSTRAINT kraken_status_update_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.kraken_status_update OWNER to postgres;

GRANT ALL ON TABLE cb.kraken_status_update TO cryptobot;
GRANT SELECT ON TABLE cb.kraken_status_update TO cryptobot_ro;
GRANT ALL ON TABLE cb.kraken_status_update TO postgres;
