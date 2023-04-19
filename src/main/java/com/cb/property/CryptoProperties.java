package com.cb.property;

import com.cb.encryption.EncryptionProvider;
import lombok.SneakyThrows;

public class CryptoProperties extends CryptoPropertiesRaw {

	private final EncryptionProvider encryptionProvider;
	
	@SneakyThrows
	public static void main(String[] a) {
		CryptoProperties properties = new CryptoProperties();
		System.out.println(properties.getJmsKrakenOrderBookSnapshotQueueName());
		System.out.println(properties.getJmsKrakenOrderBookSnapshotQueueExchange());
	}
	
	public CryptoProperties() {
		super();
		this.encryptionProvider = new EncryptionProvider();
	}

	public String getWriteDbUser() {
		return getDecryptedProperty("encrypted.db.write.user");
	}

	public String getWriteDbPassword() {
		return getDecryptedProperty("encrypted.db.write.password");
	}

	public String getReadDbUser() {
		return getDecryptedProperty("encrypted.db.read.user");
	}

	public String getReadDbPassword() {
		return getDecryptedProperty("encrypted.db.read.password");
	}

	public String getDbConnectionUrl() {
		return getDecryptedProperty("encrypted.db.connectionUrl");
	}

	public String getAlertTextNum() {
		return getDecryptedProperty("encrypted.alert.textNum");
	}
	
	public String getAlertEmail() {
		return getDecryptedProperty("encrypted.alert.email");
	}

	public String getAlertPassword() {
		return getDecryptedProperty("encrypted.alert.password");
	}

	public String getAlertSmtpHost() {
		return getDecryptedProperty("encrypted.alert.smtp.host");
	}

	public String getAlertSmtpSocketFactoryPort() {
		return getDecryptedProperty("encrypted.alert.smtp.socketFactory.port");
	}

	public String getAlertSmtpSocketFactoryClass() {
		return getDecryptedProperty("encrypted.alert.smtp.socketFactory.class");
	}

	public String getAlertSmtpAuth() {
		return getDecryptedProperty("encrypted.alert.smtp.auth");
	}

	public String getAlertSmtpPort() {
		return getDecryptedProperty("encrypted.alert.smtp.port");
	}

	public String getJmsBrokerHost() {
		return getDecryptedProperty("encrypted.jms.broker.host");
	}
	
	public int getJmsBrokerPort() {
		return Integer.parseInt(getDecryptedProperty("encrypted.jms.broker.port"));
	}
	
	public String getJmsKrakenOrderBookSnapshotQueueName() {
		return getDecryptedProperty("encrypted.jms.kraken.orderBook.snapshot.queue.name");
	}

	public String getJmsKrakenOrderBookSnapshotQueueExchange() {
		return getDecryptedProperty("encrypted.jms.kraken.orderBook.snapshot.queue.exchange");
	}

	public String getJmsUsername() {
		return getDecryptedProperty("encrypted.jms.username");
	}
	
	public String getJmsPassword() {
		return getDecryptedProperty("encrypted.jms.password");
	}

	// private methods

	private String getDecryptedProperty(String name) {
		return encryptionProvider.decrypt(properties.getProperty(name));
	}
	
}
