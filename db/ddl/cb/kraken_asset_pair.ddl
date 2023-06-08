-- Table: cb.kraken_asset_pair

-- DROP TABLE IF EXISTS cb.kraken_asset_pair;

CREATE TABLE IF NOT EXISTS cb.kraken_asset_pair
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    symbol varchar(256) NOT NULL,
    base varchar(256) NOT NULL,
    quote varchar(256) NOT NULL,
    status varchar(256) NOT NULL,
    has_index boolean NOT NULL,
    marginable boolean NOT NULL,
    margin_initial decimal,
    position_limit_long integer,
    position_limit_short integer,
    qty_min decimal NOT NULL,
    qty_precision integer NOT NULL,
    qty_increment decimal NOT NULL,
    price_precision integer NOT NULL,
    price_increment decimal NOT NULL,
    cost_min decimal NOT NULL,
    cost_precision integer NOT NULL,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone,
    CONSTRAINT kraken_asset_pair_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.kraken_asset_pair OWNER to postgres;

GRANT ALL ON TABLE cb.kraken_asset_pair TO cryptobot;
GRANT SELECT ON TABLE cb.kraken_asset_pair TO cryptobot_ro;
GRANT ALL ON TABLE cb.kraken_asset_pair TO postgres;

CREATE UNIQUE INDEX IF NOT EXISTS kraken_asset_pair_unique_payload
    ON cb.kraken_asset_pair USING btree (symbol)
    TABLESPACE pg_default;
