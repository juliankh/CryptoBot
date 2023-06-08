-- Table: cb.kraken_asset

-- DROP TABLE IF EXISTS cb.kraken_asset;

CREATE TABLE IF NOT EXISTS cb.kraken_asset
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    kraken_id varchar(256) NOT NULL,
    status varchar(256) NOT NULL,
    precision integer NOT NULL,
    precision_display integer NOT NULL,
    borrowable boolean NOT NULL,
    collateral_value decimal NOT NULL,
    margin_rate decimal,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone,
    CONSTRAINT kraken_asset_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.kraken_asset OWNER to postgres;

GRANT ALL ON TABLE cb.kraken_asset TO cryptobot;
GRANT SELECT ON TABLE cb.kraken_asset TO cryptobot_ro;
GRANT ALL ON TABLE cb.kraken_asset TO postgres;

CREATE UNIQUE INDEX IF NOT EXISTS kraken_asset_unique_payload
    ON cb.kraken_asset USING btree (kraken_id)
    TABLESPACE pg_default;