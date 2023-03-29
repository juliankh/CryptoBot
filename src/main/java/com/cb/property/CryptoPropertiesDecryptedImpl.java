package com.cb.property;

import com.cb.encryption.EncryptionProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CryptoPropertiesDecryptedImpl extends CryptoPropertiesRawImpl implements CryptoPropertiesDecrypted {
		
	private EncryptionProvider encryptionProvider;
	
	public static void main(String[] a) throws IOException {
		CryptoPropertiesDecrypted properties = new CryptoPropertiesDecryptedImpl();
		System.out.println(properties.getAlertTextNum());
	}
	
	public CryptoPropertiesDecryptedImpl() throws IOException {
		super();
		this.encryptionProvider = new EncryptionProvider();
	}
	
	private String getDecryptedProperty(String name) {
		return encryptionProvider.decrypt(properties.getProperty(name));
	}
	
	@Override
	public String getDbUser() {
		return getDecryptedProperty("encrypted.db.user");
	}

	@Override
	public String getDbPassword() {
		return getDecryptedProperty("encrypted.db.password");
	}
	
	@Override
	public String getReadDbConnectionUrl() {
		return getDecryptedProperty("encrypted.db.connectionUrl.read");
	}

	@Override
	public String getWriteDbConnectionUrl() {
		return getDecryptedProperty("encrypted.db.connectionUrl.write");
	}
	
	@Override
	public String getAlertTextNum() {
		return getDecryptedProperty("encrypted.alert.textNum");
	}
	
	@Override
	public String getAlertEmail() {
		return getDecryptedProperty("encrypted.alert.email");
	}

	@Override
	public String getAlertPassword() {
		return getDecryptedProperty("encrypted.alert.password");
	}

	@Override
	public String getAlertSmtpHost() {
		return getDecryptedProperty("encrypted.alert.smtp.host");
	}

	@Override
	public String getAlertSmtpSocketFactoryPort() {
		return getDecryptedProperty("encrypted.alert.smtp.socketFactory.port");
	}

	@Override
	public String getAlertSmtpSocketFactoryClass() {
		return getDecryptedProperty("encrypted.alert.smtp.socketFactory.class");
	}

	@Override
	public String getAlertSmtpAuth() {
		return getDecryptedProperty("encrypted.alert.smtp.auth");
	}

	@Override
	public String getAlertSmtpPort() {
		return getDecryptedProperty("encrypted.alert.smtp.port");
	}

	@Override
	public String getBinanceApiKey() {
		return getDecryptedProperty("encrypted.binance.apiKey");
	}

	@Override
	public String getBinanceApiSecret() {
		return getDecryptedProperty("encrypted.binance.apiSecret");
	}

	@Override
	public String getJmsBrokerHost() {
		return getDecryptedProperty("encrypted.jms.broker.host");
	}
	
	@Override
	public String getJmsBrokerUrl() {
		return String.format(getDecryptedProperty("encrypted.jms.broker.url"), getJmsBrokerHost());
	}
	
	@Override
	public String getJmsTopicOrderBookSnapshotBtcUsdt() {
		return getDecryptedProperty("encrypted.jms.topic.orderBook.snapshot.btcUsdt");
	}
	
	@Override
	public String getJmsTopicRecommendation() {
		return getDecryptedProperty("encrypted.jms.topic.recommendation");
	}
	
	@Override
	public String getJmsTopicCandleBtcUsdt() {
		return getDecryptedProperty("encrypted.jms.topic.candle.btcUsdt");
	}
	
	@Override
	public String getJmsTopicAggTradeBtcUsdt() {
		return getDecryptedProperty("encrypted.jms.topic.aggTrade.btcUsdt");		
	}
	
	@Override
	public String getJmsUsername() {
		return getDecryptedProperty("encrypted.jms.username");
	}
	
	@Override
	public String getJmsPassword() {
		return getDecryptedProperty("encrypted.jms.password");
	}

	@Override
	public String getJmsJettyUsername() {
		return getDecryptedProperty("encrypted.jms.jetty.username");
	}

	@Override
	public String getJmsJettyPassword() {
		return getDecryptedProperty("encrypted.jms.jetty.password");
	}

	@Override
	public Map<String, String> getJmsJettyTopicToSubscribersUrlMap() {
		Map<String, String> result = new HashMap<>();
		for (String topic : getTopicsToMonitor()) {
			result.put(topic,  String.format(getDecryptedProperty("encrypted.jms.jetty.subscribers.url"), getJmsBrokerHost(), topic));
		}
		return result;
	}
	
	private List<String> getTopicsToMonitor() {
		return Arrays.asList(getJmsTopicOrderBookSnapshotBtcUsdt());
		//return Arrays.asList(getJmsTopicOrderBookSnapshotBtcUsdt(), getJmsTopicAggTradeBtcUsdt());
		//return Arrays.asList(getJmsTopicOrderBookSnapshotBtcUsdt(), getJmsTopicAggTradeBtcUsdt(), getJmsTopicCandleBtcUsdt());
	}

	@Override
	public String getJmsJettySubscriberUrl(String subscriberObjectName) {
		return String.format(getDecryptedProperty("encrypted.jms.jetty.subscriber.url"), getJmsBrokerHost(), subscriberObjectName);
	}

	@Override
	public String getJmsTopicRecommendationAdHoc() {
		return getDecryptedProperty("encrypted.jms.topic.recommendation.adHoc");
	}

	@Override
	public String getJmsTopicRecommendationSwing() {
		return getDecryptedProperty("encrypted.jms.topic.recommendation.swing");
	}
	
}
