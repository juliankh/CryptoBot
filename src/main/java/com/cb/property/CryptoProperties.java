package com.cb.property;

import com.cb.encryption.EncryptionProcessor;
import com.cb.injection.module.CryptoBotPropertiesModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.SneakyThrows;

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
		System.out.println(cryptoProperties.jmsBrokerHost());
		System.out.println(cryptoProperties.jmsBrokerVhost());
		System.out.println(cryptoProperties.jmsBrokerPortAmqp());
		System.out.println(cryptoProperties.jmsBrokerPortHttp());
		System.out.println(cryptoProperties.jmsKrakenOrderBookSnapshotErrorQueueName());
	}

	public String writeDbUser() {
		return decryptedProperty("encrypted.db.write.user");
	}

	public String writeDbPassword() {
		return decryptedProperty("encrypted.db.write.password");
	}

	public String readDbUser() {
		return decryptedProperty("encrypted.db.read.user");
	}

	public String readDbPassword() {
		return decryptedProperty("encrypted.db.read.password");
	}

	public String dbConnectionUrl() {
		return decryptedProperty("encrypted.db.connectionUrl");
	}

	public String alertTextNum() {
		return decryptedProperty("encrypted.alert.textNum");
	}
	
	public String alertEmail() {
		return decryptedProperty("encrypted.alert.email");
	}

	public String alertPassword() {
		return decryptedProperty("encrypted.alert.password");
	}

	public String alertSmtpHost() {
		return decryptedProperty("encrypted.alert.smtp.host");
	}

	public String alertSmtpSocketFactoryPort() {
		return decryptedProperty("encrypted.alert.smtp.socketFactory.port");
	}

	public String alertSmtpSocketFactoryClass() {
		return decryptedProperty("encrypted.alert.smtp.socketFactory.class");
	}

	public String alertSmtpAuth() {
		return decryptedProperty("encrypted.alert.smtp.auth");
	}

	public String alertSmtpPort() {
		return decryptedProperty("encrypted.alert.smtp.port");
	}

	public String jmsBrokerHost() {
		return decryptedProperty("encrypted.jms.broker.host");
	}

	public String jmsBrokerVhost() {
		return decryptedProperty("encrypted.jms.broker.vhost");
	}

	public int jmsBrokerPortAmqp() {
		return Integer.parseInt(decryptedProperty("encrypted.jms.broker.port.amqp"));
	}

	public int jmsBrokerPortHttp() {
		return Integer.parseInt(decryptedProperty("encrypted.jms.broker.port.http"));
	}

	public String jmsUsername() {
		return decryptedProperty("encrypted.jms.username");
	}

	public String jmsPassword() {
		return decryptedProperty("encrypted.jms.password");
	}
	
	public String jmsKrakenOrderBookSnapshotQueueName() {
		return decryptedProperty("encrypted.jms.kraken.orderBook.snapshot.queue.name");
	}

	public String jmsKrakenOrderBookSnapshotErrorQueueName() {
		return decryptedProperty("encrypted.jms.kraken.orderBook.snapshot.error_queue.name");
	}

	public String jmsKrakenOrderBookSnapshotQueueExchange() {
		return decryptedProperty("encrypted.jms.kraken.orderBook.snapshot.queue.exchange");
	}

	// private methods

	private String decryptedProperty(String name) {
		return encryptionProcessor.decrypt(properties.getProperty(name));
	}
	
}
