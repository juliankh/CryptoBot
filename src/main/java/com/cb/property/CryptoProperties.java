package com.cb.property;

import com.cb.encryption.EncryptionProvider;
import lombok.SneakyThrows;

public class CryptoProperties extends CryptoPropertiesRaw {

	private final EncryptionProvider encryptionProvider;
	
	@SneakyThrows
	public static void main(String[] a) {
		CryptoProperties properties = new CryptoProperties();
		System.out.println(properties.jmsKrakenOrderBookSnapshotErrorQueueName());
	}
	
	public CryptoProperties() {
		super();
		this.encryptionProvider = new EncryptionProvider();
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
	
	public int jmsBrokerPort() {
		return Integer.parseInt(decryptedProperty("encrypted.jms.broker.port"));
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
		return encryptionProvider.decrypt(properties.getProperty(name));
	}
	
}
