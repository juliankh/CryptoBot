-- Table: cb.jms_destination_stats

-- DROP TABLE IF EXISTS cb.jms_destination_stats;

CREATE TABLE IF NOT EXISTS cb.jms_destination_stats
(
    id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    name varchar(256) NOT NULL,
    measured timestamp with time zone NOT NULL,
    messages integer NOT NULL,
    consumers integer NOT NULL,
    created timestamp with time zone NOT NULL,
    CONSTRAINT jms_destination_stats_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS cb.jms_destination_stats OWNER to postgres;
GRANT ALL ON TABLE cb.jms_destination_stats TO cryptobot;
GRANT SELECT ON TABLE cb.jms_destination_stats TO cryptobot_ro;
GRANT ALL ON TABLE cb.jms_destination_stats TO postgres;