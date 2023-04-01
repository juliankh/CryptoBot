package com.cb.property;

import java.util.Map;

public interface CryptoPropertiesDecrypted extends CryptoPropertiesRaw {

	String getWriteDbUser();
	String getWriteDbPassword();

	String getReadDbUser();
	String getReadDbPassword();

	String getDbConnectionUrl();
	
	String getAlertTextNum();
	String getAlertEmail();
	String getAlertPassword(); // this should be a generated App Password (not the default google account password) as per: https://mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/
	String getAlertSmtpHost();
	String getAlertSmtpSocketFactoryPort();
	String getAlertSmtpSocketFactoryClass();
	String getAlertSmtpAuth();
	String getAlertSmtpPort();	
	
	String getBinanceApiKey();
	String getBinanceApiSecret();
	
	String getJmsBrokerHost();
	String getJmsBrokerUrl();
	String getJmsUsername();
	String getJmsPassword();
	String getJmsTopicOrderBookSnapshotBtcUsdt();
	String getJmsTopicRecommendation();
	String getJmsTopicRecommendationAdHoc();
	String getJmsTopicRecommendationSwing();
	String getJmsTopicCandleBtcUsdt();
	String getJmsTopicAggTradeBtcUsdt();
	String getJmsJettyUsername();
	String getJmsJettyPassword();
	Map<String, String> getJmsJettyTopicToSubscribersUrlMap();
	String getJmsJettySubscriberUrl(String subscriberObjectName);
	
}
