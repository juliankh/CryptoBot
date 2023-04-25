-- Table: cb.kraken_orderbook_btc_usdt

-- DROP TABLE IF EXISTS cb.kraken_orderbook_btc_usdt;

CREATE TABLE IF NOT EXISTS cb.kraken_orderbook_btc_usdt
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    process varchar(256) NOT NULL,
    exchange_datetime timestamp with time zone NOT NULL,
    exchange_date date NOT NULL,
    received_nanos bigint NOT NULL,
    created timestamp with time zone NOT NULL,
    highest_bid_price numeric NOT NULL,
    highest_bid_volume numeric NOT NULL,
    lowest_ask_price numeric NOT NULL,
    lowest_ask_volume numeric NOT NULL,
    bids_hash bigint NOT NULL,
    asks_hash bigint NOT NULL,
    bids orderbook_quote[] NOT NULL,
    asks orderbook_quote[] NOT NULL,
    CONSTRAINT kraken_orderbook_btc_usdt_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.kraken_orderbook_btc_usdt OWNER to postgres;
GRANT ALL ON TABLE cb.kraken_orderbook_btc_usdt TO cryptobot;
GRANT SELECT ON TABLE cb.kraken_orderbook_btc_usdt TO cryptobot_ro;
GRANT ALL ON TABLE cb.kraken_orderbook_btc_usdt TO postgres;
-- Index: kraken_orderbook_btc_usdt_exchange_date_index

-- DROP INDEX IF EXISTS cb.kraken_orderbook_btc_usdt_exchange_date_index;

CREATE INDEX IF NOT EXISTS kraken_orderbook_btc_usdt_exchange_date_index ON cb.kraken_orderbook_btc_usdt USING btree (exchange_date DESC NULLS FIRST) TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.kraken_orderbook_btc_usdt CLUSTER ON kraken_orderbook_btc_usdt_exchange_date_index;
-- Index: kraken_orderbook_btc_usdt_exchange_datetime_index

-- DROP INDEX IF EXISTS cb.kraken_orderbook_btc_usdt_exchange_datetime_index;

CREATE INDEX IF NOT EXISTS kraken_orderbook_btc_usdt_exchange_datetime_index
    ON cb.kraken_orderbook_btc_usdt USING btree
    (exchange_datetime DESC NULLS FIRST)
    TABLESPACE pg_default;

CREATE UNIQUE INDEX IF NOT EXISTS kraken_orderbook_btc_usdt_unique_payload
    ON cb.kraken_orderbook_btc_usdt USING btree (received_nanos DESC NULLS FIRST, bids_hash DESC NULLS FIRST, asks_hash DESC NULLS FIRST)
    TABLESPACE pg_default;