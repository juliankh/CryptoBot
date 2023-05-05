package com.cb.module;

import com.cb.driver.admin.DataAgeMonitorDriver;
import com.cb.driver.admin.DataCleanerDriver;
import com.cb.driver.admin.DiskSpaceMonitorDriver;
import com.cb.driver.admin.JmsQueueMonitorDriver;
import com.cb.driver.kraken.KrakenOrderBookBridgeDriver;
import com.cb.driver.kraken.KrakenOrderBookPersisterDriver;
import com.cb.encryption.EncryptionProvider;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import com.cb.module.provider.KrakenOrderBookPersistJmsConsumerProvider;
import com.cb.module.provider.ListProvider;
import com.cb.property.CryptoProperties;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

import static com.cb.module.BindingName.*;

@Slf4j
public class CryptoBotModule extends AbstractModule {

    public static Injector INJECTOR = Guice.createInjector(new CryptoBotModule());

    @Override
    protected void configure() {
        Properties properties = properties();

        String mainDir = properties.getProperty("mainDir");
        String encryptionSubDir = properties.getProperty("encryption.subDir");
        String keyFile = properties.getProperty("encryption.keyFile");
        String encryptionKeyFilePath = mainDir + encryptionSubDir + keyFile;

        CryptoProperties cryptoProperties = cryptoProperties(properties, encryptionKeyFilePath);

        bindConstant().annotatedWith(Names.named(ENCRYPTION_KEY_FILE_PATH)).to(encryptionKeyFilePath);
        bindConstant().annotatedWith(Names.named(DB_CONNECTION_URL)).to(cryptoProperties.dbConnectionUrl());
        bindConstant().annotatedWith(Names.named(DB_READ_USER)).to(cryptoProperties.readDbUser());
        bindConstant().annotatedWith(Names.named(DB_READ_PASSWORD)).to(cryptoProperties.readDbPassword());
        bindConstant().annotatedWith(Names.named(DB_WRITE_USER)).to(cryptoProperties.writeDbUser());
        bindConstant().annotatedWith(Names.named(DB_WRITE_PASSWORD)).to(cryptoProperties.writeDbPassword());

        bindConstant().annotatedWith(Names.named(JMS_BROKER_HOST)).to(cryptoProperties.jmsBrokerHost());
        bindConstant().annotatedWith(Names.named(JMS_BROKER_PORT)).to(cryptoProperties.jmsBrokerPortAmqp());
        bindConstant().annotatedWith(Names.named(JMS_VHOST)).to(cryptoProperties.jmsBrokerVhost());
        bindConstant().annotatedWith(Names.named(JMS_API_URL)).to("http://" + cryptoProperties.jmsBrokerHost() + ":" + cryptoProperties.jmsBrokerPortHttp() + "/api/");
        bindConstant().annotatedWith(Names.named(JMS_USERNAME)).to(cryptoProperties.jmsUsername());
        bindConstant().annotatedWith(Names.named(JMS_PASSWORD)).to(cryptoProperties.jmsPassword());

        bindConstant().annotatedWith(Names.named(JMS_KRAKEN_ORDERBOOK_SNAPSHOT_QUEUE)).to(cryptoProperties.jmsKrakenOrderBookSnapshotQueueName());
        bindConstant().annotatedWith(Names.named(JMS_KRAKEN_ORDERBOOK_SNAPSHOT_EXCHANGE)).to(cryptoProperties.jmsKrakenOrderBookSnapshotQueueExchange());

        bind(new TypeLiteral<List<KrakenOrderBookPersistJmsConsumer>>() {}).toProvider(new ListProvider<>(new KrakenOrderBookPersistJmsConsumerProvider(cryptoProperties.jmsKrakenOrderBookSnapshotQueueName()), 30));

        bind(KrakenOrderBookBridgeDriver.class);
        bind(KrakenOrderBookPersisterDriver.class);
        bind(DataAgeMonitorDriver.class);
        bind(DataCleanerDriver.class);
        bind(DiskSpaceMonitorDriver.class);
        bind(JmsQueueMonitorDriver.class);
    }

    @Provides
    public CryptoProperties cryptoProperties(Properties properties, @Named(ENCRYPTION_KEY_FILE_PATH) String encryptionKeyFilePath) {
        EncryptionProvider encryptionProvider = new EncryptionProvider(encryptor(encryptionKeyFilePath));
        return new CryptoProperties(properties, encryptionProvider);
    }

    @Provides
    @Named(DB_READ_CONNECTION)
    @SneakyThrows
    public java.sql.Connection readConnection(@Named(DB_CONNECTION_URL) String dbConnectionUrl, @Named(DB_READ_USER) String readDbUser, @Named(DB_READ_PASSWORD) String readDbPassword) {
        return DriverManager.getConnection(dbConnectionUrl, readDbUser, readDbPassword);
    }

    @Provides
    @Named(DB_WRITE_CONNECTION)
    @SneakyThrows
    public java.sql.Connection writeConnection(@Named(DB_CONNECTION_URL) String dbConnectionUrl, @Named(DB_WRITE_USER) String writeDbUser, @Named(DB_WRITE_PASSWORD) String writeDbPassword) {
        return DriverManager.getConnection(dbConnectionUrl, writeDbUser, writeDbPassword);
    }

    @Provides
    @SneakyThrows
    public Client jmsClient(@Named(JMS_API_URL) String url, @Named(JMS_USERNAME) String username, @Named(JMS_PASSWORD) String password) {
        return new Client(new ClientParameters().url(url).username(username).password(password));
    }

    @Provides
    public ConnectionFactory jmsConnectionFactory(@Named(JMS_BROKER_HOST) String brokerHost, @Named(JMS_BROKER_PORT) int brokerPort, @Named(JMS_USERNAME) String username, @Named(JMS_PASSWORD) String password) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(brokerHost);
        factory.setPort(brokerPort);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    // --------------------------  PRIVATE METHODS  --------------------------

    @SneakyThrows
    private Properties properties() {
        Properties properties = new Properties();
        properties.load(new FileInputStream("property/crypto.properties"));
        return properties;
    }

    @SneakyThrows
    private StandardPBEStringEncryptor encryptor(String encryptionKeyFilePath) {
        String encryptionKey = FileUtils.readFileToString(new File(encryptionKeyFilePath), StandardCharsets.ISO_8859_1);
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionKey);
        encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
        return encryptor;
    }

}
