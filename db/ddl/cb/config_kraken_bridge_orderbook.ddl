-- Table: cb.config_kraken_bridge_orderbook

-- DROP TABLE IF EXISTS cb.config_kraken_bridge_orderbook;

CREATE TABLE IF NOT EXISTS cb.config_kraken_bridge_orderbook
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    currency_base varchar(256) NOT NULL,
    currency_counter varchar(256) NOT NULL,
    batch_size integer NOT NULL,
    secs_timeout integer NOT NULL,
    CONSTRAINT config_kraken_bridge_orderbook_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.config_kraken_bridge_orderbook OWNER to postgres;

GRANT ALL ON TABLE cb.config_kraken_bridge_orderbook TO cryptobot;
GRANT SELECT ON TABLE cb.config_kraken_bridge_orderbook TO cryptobot_ro;
GRANT ALL ON TABLE cb.config_kraken_bridge_orderbook TO postgres;