package com.cb.injection;

public interface BindingName {

    String MAIN_DIR = "mainDir";
    String ENCRYPTION_SUBDIR = "encryption.subDir";
    String ENCRYPTION_KEY_FILE = "encryption.keyFile";

    String DB_CONNECTION_URL = "dbConnectionUrl";

    String DB_READ_CONNECTION = "readConnection";
    String DB_READ_USER = "readDbUser";
    String DB_READ_PASSWORD = "readDbPassword";

    String DB_WRITE_CONNECTION = "writeConnection";
    String DB_WRITE_USER = "writeDbUser";
    String DB_WRITE_PASSWORD = "writeDbPassword";

    String ALERT_EMAIL = "alertEmail";
    String ALERT_PASSWORD = "alertPassword";
    String ALERT_TEXT_NUM = "alertTextNum";
    String ALERT_SMTP_HOST = "alertSmtpHost";
    String ALERT_SMTP_SOCKET_FACTORY_PORT = "alertSmtpSocketFactoryPort";
    String ALERT_SMTP_SOCKET_FACTORY_CLASS = "alertSmtpSocketFactoryClass";
    String ALERT_SMTP_AUTH = "alertSmtpAuth";
    String ALERT_SMTP_PORT = "alertSmtpPort";

    // TODO: if there is no use case to use jms, then remove jms-related code
    String JMS_BROKER_HOST = "jmsBrokerHost";
    String JMS_BROKER_PORT = "jmsBrokerPort";
    String JMS_API_URL = "jmsApiUrl";
    String JMS_VHOST = "jmsVhost";
    String JMS_USERNAME = "jmsUsername";
    String JMS_PASSWORD = "jmsPassword";
    String JMS_KRAKEN_ORDERBOOK_SNAPSHOT_QUEUE = "jmsKrakenOrderBookSnapshotQueue";
    String JMS_KRAKEN_ORDERBOOK_SNAPSHOT_EXCHANGE = "jmsKrakenOrderBookSnapshotExchange";

    String REDIS_HOST = "redisHost";
    String REDIS_PORT = "redisPort";

    String KRAKEN_WEBSOCKET_V2_URL = "krakenWebSocketV2Url";
    String KRAKEN_WEBSOCKET_V2_CLIENT_ORDER_BOOK = "krakenWebSocketV2ClientOrderBook";
    String KRAKEN_WEBSOCKET_V2_CLIENT_INSTRUMENT = "krakenWebSocketV2ClientInstrument";

    String KRAKEN_CHECKSUM_CALCULATOR = "krakenChecksumCalculator";

}
