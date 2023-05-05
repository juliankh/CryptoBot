package com.cb.module;

public interface BindingName {

    String ENCRYPTION_KEY_FILE_PATH = "encryptionKeyFilePath";

    String DB_CONNECTION_URL = "dbConnectionUrl";

    String DB_READ_CONNECTION = "readConnection";
    String DB_READ_USER = "readDbUser";
    String DB_READ_PASSWORD = "readDbPassword";

    String DB_WRITE_CONNECTION = "writeConnection";
    String DB_WRITE_USER = "writeDbUser";
    String DB_WRITE_PASSWORD = "writeDbPassword";

    String JMS_BROKER_HOST = "jmsBrokerHost";
    String JMS_BROKER_PORT = "jmsBrokerPort";
    String JMS_API_URL = "jmsApiUrl";
    String JMS_VHOST = "jmsVhost";
    String JMS_USERNAME = "jmsUsername";
    String JMS_PASSWORD = "jmsPassword";

    String JMS_KRAKEN_ORDERBOOK_SNAPSHOT_QUEUE = "jmsKrakenOrderBookSnapshotQueue";
    String JMS_KRAKEN_ORDERBOOK_SNAPSHOT_EXCHANGE = "jmsKrakenOrderBookSnapshotExchange";

}
