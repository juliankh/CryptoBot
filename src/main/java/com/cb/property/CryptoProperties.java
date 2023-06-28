package com.cb.property;

import com.cb.common.EncryptionProcessor;
import com.cb.injection.module.CryptoBotPropertiesModule;
import lombok.SneakyThrows;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;

@Singleton
public class CryptoProperties {

	@Inject
	private Properties properties;

	@Inject
	private EncryptionProcessor encryptionProcessor;

	@SneakyThrows
	public static void main(String[] a) {
		CryptoProperties cryptoProperties = CryptoBotPropertiesModule.INJECTOR.getInstance(CryptoProperties.class);
		System.out.println(cryptoProperties.krakenWebSocketV2Url());
	}

	public String writeDbUser() {
		return decryptedProperty("db.write.user");
	}

	public String writeDbPassword() {
		return decryptedProperty("db.write.password");
	}

	public String readDbUser() {
		return decryptedProperty("db.read.user");
	}

	public String readDbPassword() {
		return decryptedProperty("db.read.password");
	}

	public String dbConnectionUrl() {
		return decryptedProperty("db.connectionUrl");
	}

	public String alertTextNum() {
		return decryptedProperty("alert.textNum");
	}
	
	public String alertEmail() {
		return decryptedProperty("alert.email");
	}

	public String alertPassword() {
		return decryptedProperty("alert.password");
	}

	public String alertSmtpHost() {
		return decryptedProperty("alert.smtp.host");
	}

	public String alertSmtpSocketFactoryPort() {
		return decryptedProperty("alert.smtp.socketFactory.port");
	}

	public String alertSmtpSocketFactoryClass() {
		return decryptedProperty("alert.smtp.socketFactory.class");
	}

	public String alertSmtpAuth() {
		return decryptedProperty("alert.smtp.auth");
	}

	public String alertSmtpPort() {
		return decryptedProperty("alert.smtp.port");
	}

	public String jmsBrokerHost() {
		return decryptedProperty("jms.broker.host");
	}

	public String jmsBrokerVhost() {
		return decryptedProperty("jms.broker.vhost");
	}

	public int jmsBrokerPortAmqp() {
		return Integer.parseInt(decryptedProperty("jms.broker.port.amqp"));
	}

	public int jmsBrokerPortHttp() {
		return Integer.parseInt(decryptedProperty("jms.broker.port.http"));
	}

	public String jmsUsername() {
		return decryptedProperty("jms.username");
	}

	public String jmsPassword() {
		return decryptedProperty("jms.password");
	}
	
	public String jmsKrakenOrderBookSnapshotQueueName() {
		return decryptedProperty("jms.kraken.orderBook.snapshot.queue.name");
	}

	public String jmsKrakenOrderBookSnapshotErrorQueueName() {
		return decryptedProperty("jms.kraken.orderBook.snapshot.error_queue.name");
	}

	public String jmsKrakenOrderBookSnapshotQueueExchange() {
		return decryptedProperty("jms.kraken.orderBook.snapshot.queue.exchange");
	}

	public String redisHost() {
		return decryptedProperty("redis.host");
	}

	public int redisPort() {
		return Integer.parseInt(decryptedProperty("redis.port"));
	}

	public String krakenWebSocketV2Url() {
		return decryptedProperty("kraken.websocket.v2.url");
	}

	// private methods

	private String decryptedProperty(String name) {
		return encryptionProcessor.decrypt(properties.getProperty(name));
	}
	
}
