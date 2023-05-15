-- Table: cb.kraken_orderbook_atom_usd

-- DROP TABLE IF EXISTS cb.kraken_orderbook_atom_usd;

CREATE TABLE IF NOT EXISTS cb.kraken_orderbook_atom_usd
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    process varchar(256) NOT NULL,
    exchange_datetime timestamp with time zone NOT NULL,
    exchange_date date NOT NULL,
    received_micros bigint NOT NULL,
    created timestamp with time zone NOT NULL,
    highest_bid_price numeric NOT NULL,
    highest_bid_volume numeric NOT NULL,
    lowest_ask_price numeric NOT NULL,
    lowest_ask_volume numeric NOT NULL,
    bids_hash bigint NOT NULL,
    asks_hash bigint NOT NULL,
    bids orderbook_quote[] NOT NULL,
    asks orderbook_quote[] NOT NULL,
    CONSTRAINT kraken_orderbook_atom_usd_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.kraken_orderbook_atom_usd OWNER to postgres;
GRANT ALL ON TABLE cb.kraken_orderbook_atom_usd TO cryptobot;
GRANT SELECT ON TABLE cb.kraken_orderbook_atom_usd TO cryptobot_ro;
GRANT ALL ON TABLE cb.kraken_orderbook_atom_usd TO postgres;
-- Index: kraken_orderbook_atom_usd_exchange_date_index

-- DROP INDEX IF EXISTS cb.kraken_orderbook_atom_usd_exchange_date_index;

CREATE INDEX IF NOT EXISTS kraken_orderbook_atom_usd_exchange_date_index ON cb.kraken_orderbook_atom_usd USING btree (exchange_date DESC NULLS FIRST) TABLESPACE pg_default;
ALTER TABLE IF EXISTS cb.kraken_orderbook_atom_usd CLUSTER ON kraken_orderbook_atom_usd_exchange_date_index;
-- Index: kraken_orderbook_atom_usd_exchange_datetime_index

-- DROP INDEX IF EXISTS cb.kraken_orderbook_atom_usd_exchange_datetime_index;

CREATE INDEX IF NOT EXISTS kraken_orderbook_atom_usd_exchange_datetime_index
    ON cb.kraken_orderbook_atom_usd USING btree
    (exchange_datetime DESC NULLS FIRST)
    TABLESPACE pg_default;

CREATE UNIQUE INDEX IF NOT EXISTS kraken_orderbook_atom_usd_unique_payload
    ON cb.kraken_orderbook_atom_usd USING btree (received_micros DESC NULLS FIRST, bids_hash DESC NULLS FIRST, asks_hash DESC NULLS FIRST)
    TABLESPACE pg_default;