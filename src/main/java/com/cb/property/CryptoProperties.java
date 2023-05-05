package com.cb.property;

import com.cb.encryption.EncryptionProvider;
import com.cb.module.CryptoBotModule;
import lombok.SneakyThrows;

import java.util.Properties;

public class CryptoProperties {

	private final Properties properties;

	private final EncryptionProvider encryptionProvider;

	public CryptoProperties(Properties properties, EncryptionProvider encryptionProvider) {
		this.properties = properties;
		this.encryptionProvider = encryptionProvider;
	}

	@SneakyThrows
	public static void main(String[] a) {
		CryptoProperties properties = CryptoBotModule.INJECTOR.getInstance(CryptoProperties.class);
		System.out.println(properties.jmsBrokerHost());
		System.out.println(properties.jmsBrokerVhost());
		System.out.println(properties.jmsBrokerPortAmqp());
		System.out.println(properties.jmsBrokerPortHttp());
		System.out.println(properties.jmsKrakenOrderBookSnapshotErrorQueueName());
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

	public Properties emailProperties() {
		// TODO: perhaps make this into a class field that is lazy-loaded and subsequently reused (and make this method synchronized)
		Properties emailProperties = new Properties();
		emailProperties.put("mail.smtp.host", alertSmtpHost());
		emailProperties.put("mail.smtp.socketFactory.port", alertSmtpSocketFactoryPort());
		emailProperties.put("mail.smtp.socketFactory.class", alertSmtpSocketFactoryClass());
		emailProperties.put("mail.smtp.auth", alertSmtpAuth());
		emailProperties.put("mail.smtp.port", alertSmtpPort());
		return emailProperties;
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
		return encryptionProvider.decrypt(properties.getProperty(name));
	}
	
}
