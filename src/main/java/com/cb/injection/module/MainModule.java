package com.cb.injection.module;

import com.cb.db.DbReadOnlyProvider;
import com.cb.injection.provider.KrakenOrderBookPersistJmsConsumerProvider;
import com.cb.injection.provider.ListProvider;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import com.cb.property.CryptoProperties;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.DriverManager;
import java.util.List;

import static com.cb.injection.BindingName.*;

@Slf4j
public class MainModule extends AbstractModule {

    public static Injector INJECTOR = Guice.createInjector(new MainModule());

    @Override
    protected void configure() {
        CryptoProperties cryptoProperties = CryptoBotPropertiesModule.INJECTOR.getInstance(CryptoProperties.class);

        bindConstant().annotatedWith(Names.named(DB_CONNECTION_URL)).to(cryptoProperties.dbConnectionUrl());
        bindConstant().annotatedWith(Names.named(DB_READ_USER)).to(cryptoProperties.readDbUser());
        bindConstant().annotatedWith(Names.named(DB_READ_PASSWORD)).to(cryptoProperties.readDbPassword());
        bindConstant().annotatedWith(Names.named(DB_WRITE_USER)).to(cryptoProperties.writeDbUser());
        bindConstant().annotatedWith(Names.named(DB_WRITE_PASSWORD)).to(cryptoProperties.writeDbPassword());

        bindConstant().annotatedWith(Names.named(ALERT_EMAIL)).to(cryptoProperties.alertEmail());
        bindConstant().annotatedWith(Names.named(ALERT_PASSWORD)).to(cryptoProperties.alertPassword());
        bindConstant().annotatedWith(Names.named(ALERT_TEXT_NUM)).to(cryptoProperties.alertTextNum());
        bindConstant().annotatedWith(Names.named(ALERT_SMTP_HOST)).to(cryptoProperties.alertSmtpHost());
        bindConstant().annotatedWith(Names.named(ALERT_SMTP_SOCKET_FACTORY_PORT)).to(cryptoProperties.alertSmtpSocketFactoryPort());
        bindConstant().annotatedWith(Names.named(ALERT_SMTP_SOCKET_FACTORY_CLASS)).to(cryptoProperties.alertSmtpSocketFactoryClass());
        bindConstant().annotatedWith(Names.named(ALERT_SMTP_AUTH)).to(cryptoProperties.alertSmtpAuth());
        bindConstant().annotatedWith(Names.named(ALERT_SMTP_PORT)).to(cryptoProperties.alertSmtpPort());

        bindConstant().annotatedWith(Names.named(JMS_BROKER_HOST)).to(cryptoProperties.jmsBrokerHost());
        bindConstant().annotatedWith(Names.named(JMS_BROKER_PORT)).to(cryptoProperties.jmsBrokerPortAmqp());
        bindConstant().annotatedWith(Names.named(JMS_VHOST)).to(cryptoProperties.jmsBrokerVhost());
        bindConstant().annotatedWith(Names.named(JMS_API_URL)).to("http://" + cryptoProperties.jmsBrokerHost() + ":" + cryptoProperties.jmsBrokerPortHttp() + "/api/");
        bindConstant().annotatedWith(Names.named(JMS_USERNAME)).to(cryptoProperties.jmsUsername());
        bindConstant().annotatedWith(Names.named(JMS_PASSWORD)).to(cryptoProperties.jmsPassword());

        bindConstant().annotatedWith(Names.named(JMS_KRAKEN_ORDERBOOK_SNAPSHOT_QUEUE)).to(cryptoProperties.jmsKrakenOrderBookSnapshotQueueName());
        bindConstant().annotatedWith(Names.named(JMS_KRAKEN_ORDERBOOK_SNAPSHOT_EXCHANGE)).to(cryptoProperties.jmsKrakenOrderBookSnapshotQueueExchange());

        bind(new TypeLiteral<List<KrakenOrderBookPersistJmsConsumer>>() {}).toProvider(new ListProvider<>(new KrakenOrderBookPersistJmsConsumerProvider(cryptoProperties.jmsKrakenOrderBookSnapshotQueueName()), 60));
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

}
