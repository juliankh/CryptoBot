package com.cb.alert;

import com.cb.property.CryptoPropertiesDecrypted;
import com.cb.property.CryptoPropertiesDecryptedImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class AlertProviderImpl implements AlertProvider {

	public static boolean DEFAULT_IS_ON = true;	
	
	private final CryptoPropertiesDecrypted cryptoProperties;
	private final Properties emailProperties;
	private final boolean isOn;
	
	public static void main(String[] args) throws IOException {
		Throwable t = null;
		try {
			try {
				throw new Exception("hey hey");
			} catch (Exception e) {
				throw new IllegalArgumentException("another 2", e);
			}
		} catch (Exception e) {
			t = e;
		}
		(new AlertProviderImpl()).sendEmailAlert("subj", "bodbod", t);
		//(new AlertProviderImpl()).sendTextAlert("hey hello");
	}

	public AlertProviderImpl() throws IOException {
		this(DEFAULT_IS_ON);
	}
	
	public AlertProviderImpl(boolean isOn) throws IOException {
		this.isOn = isOn;
		this.cryptoProperties = new CryptoPropertiesDecryptedImpl();
		this.emailProperties = new Properties();
		this.emailProperties.put("mail.smtp.host", cryptoProperties.getAlertSmtpHost());
		this.emailProperties.put("mail.smtp.socketFactory.port", cryptoProperties.getAlertSmtpSocketFactoryPort());
		this.emailProperties.put("mail.smtp.socketFactory.class", cryptoProperties.getAlertSmtpSocketFactoryClass());
		this.emailProperties.put("mail.smtp.auth", cryptoProperties.getAlertSmtpAuth());
		this.emailProperties.put("mail.smtp.port", cryptoProperties.getAlertSmtpPort());
	}
	
	@Override
	public void sendEmailAlert(String subject, String body, Throwable t) {
		sendEmailAlert(subject, body + "\n\n" + ExceptionUtils.getStackTrace(t));
	}
	
	@Override
	public void sendEmailAlert(String subject, String body) {
		sendAlert(subject, body, cryptoProperties.getAlertEmail());
	}
	
	@Override
	public void sendTextAlert(String msg) {
		sendAlert(msg, msg, cryptoProperties.getAlertTextNum());
	}

	@Override
	public void sendAlert(String subject, String body, String recipient) {
		if (!isOn) {
			log.info("NOT Sending alert with subject [" + subject + "] because the Alert system is OFF");	
			return;
		}
		log.info("Sending alert with subject [" + subject + "]");
		Session session = Session.getDefaultInstance(emailProperties,
			new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(cryptoProperties.getAlertEmail(), cryptoProperties.getAlertPassword()); 
				}
			});
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(cryptoProperties.getAlertEmail()));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
			message.setSubject(subject);
			message.setText(body);
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException("Problem sending alert with subject [" + subject + "]", e);
		}
	}
	
}
