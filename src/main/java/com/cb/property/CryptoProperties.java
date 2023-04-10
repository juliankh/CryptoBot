package com.cb.property;

import com.cb.encryption.EncryptionProvider;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public String getBinanceApiKey() {
		return getDecryptedProperty("encrypted.binance.apiKey");
	}

	public String getBinanceApiSecret() {
		return getDecryptedProperty("encrypted.binance.apiSecret");
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

	public String getJmsTopicRecommendation() {
		return getDecryptedProperty("encrypted.jms.topic.recommendation");
	}
	
	public String getJmsTopicCandleBtcUsdt() {
		return getDecryptedProperty("encrypted.jms.topic.candle.btcUsdt");
	}
	
	public String getJmsTopicAggTradeBtcUsdt() {
		return getDecryptedProperty("encrypted.jms.topic.aggTrade.btcUsdt");		
	}
	
	public String getJmsUsername() {
		return getDecryptedProperty("encrypted.jms.username");
	}
	
	public String getJmsPassword() {
		return getDecryptedProperty("encrypted.jms.password");
	}

	public String getJmsJettyUsername() {
		return getDecryptedProperty("encrypted.jms.jetty.username");
	}

	public String getJmsJettyPassword() {
		return getDecryptedProperty("encrypted.jms.jetty.password");
	}

	public Map<String, String> getJmsJettyTopicToSubscribersUrlMap() {
		Map<String, String> result = new HashMap<>();
		for (String topic : getTopicsToMonitor()) {
			result.put(topic,  String.format(getDecryptedProperty("encrypted.jms.jetty.subscribers.url"), getJmsBrokerHost(), topic));
		}
		return result;
	}
	
	private List<String> getTopicsToMonitor() {
		return Arrays.asList(getJmsKrakenOrderBookSnapshotQueueName());
		//return Arrays.asList(getJmsTopicOrderBookSnapshotBtcUsdt(), getJmsTopicAggTradeBtcUsdt());
		//return Arrays.asList(getJmsTopicOrderBookSnapshotBtcUsdt(), getJmsTopicAggTradeBtcUsdt(), getJmsTopicCandleBtcUsdt());
	}

	public String getJmsJettySubscriberUrl(String subscriberObjectName) {
		return String.format(getDecryptedProperty("encrypted.jms.jetty.subscriber.url"), getJmsBrokerHost(), subscriberObjectName);
	}

	public String getJmsTopicRecommendationAdHoc() {
		return getDecryptedProperty("encrypted.jms.topic.recommendation.adHoc");
	}

	public String getJmsTopicRecommendationSwing() {
		return getDecryptedProperty("encrypted.jms.topic.recommendation.swing");
	}
	
	// private methods

	private String getDecryptedProperty(String name) {
		return encryptionProvider.decrypt(properties.getProperty(name));
	}
	
}
