package com.cb.alert;

import com.cb.property.CryptoProperties;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

@Slf4j
@Singleton
public class AlertProvider {

	public static boolean DEFAULT_IS_ON = true;

	@Inject
	private CryptoProperties cryptoProperties;

	/* // for manual testing
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
		(new AlertProvider()).sendEmailAlert("subj", "bodbod", t);
		//(new AlertProviderImpl()).sendTextAlert("hey hello");
	}*/

	// DO NOT MODIFY/DELETE -- this is used by safety net driver wrapper scripts
	public static void main(String[] args) throws IOException {
		(new AlertProvider()).sendEmailAlert(args[0], args[1]);
	}

	public void sendEmailAlert(String subject, String body, Throwable t) {
		sendEmailAlert(subject, body, t, false);
	}

	public void sendEmailAlertQuietly(String subject, String body, Throwable t) {
		sendEmailAlert(subject, body, t, true);
	}

	public void sendEmailAlert(String subject, String body, Throwable t, boolean quietly) {
		sendEmailAlert(subject, body + "\n\n" + ExceptionUtils.getStackTrace(t), quietly);
	}

	public void sendEmailAlert(String subject, String body) {
		sendEmailAlert(subject, body, false);
	}

	public void sendEmailAlertQuietly(String subject, String body) {
		sendEmailAlert(subject, body, true);
	}

	public void sendEmailAlert(String subject, String body, boolean quietly) {
		sendAlert(subject, body, cryptoProperties.alertEmail(), quietly);
	}

	public void sendTextAlert(String msg) {
		sendTextAlert(msg, false);
	}

	public void sendTextAlertQuietly(String msg) {
		sendTextAlert(msg, true);
	}

	public void sendTextAlert(String msg, boolean quietly) {
		sendAlert(msg, msg, cryptoProperties.alertTextNum(), quietly);
	}

	public void sendAlert(String subject, String body, String recipient, boolean quietly) {
		try {
			if (!DEFAULT_IS_ON) {
				log.info("NOT Sending alert with subject [" + subject + "] because the Alert system is OFF");
				return;
			}
			log.info("Sending alert with subject [" + subject + "]");
			Session session = Session.getDefaultInstance(cryptoProperties.emailProperties(),
					new Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(cryptoProperties.alertEmail(), cryptoProperties.alertPassword());
						}
					});
			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(cryptoProperties.alertEmail()));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
				message.setSubject(subject);
				message.setText(body);
				Transport.send(message);
			} catch (MessagingException e) {
				throw new RuntimeException("Problem sending alert with subject [" + subject + "]", e);
			}
		} catch (Exception e) {
			if (quietly) {
				log.error("Problem sending alert with subject [" + subject + "].  Logging, but otherwise ignoring because the quietly flag is ON.", e);
			} else {
				throw e;
			}
		}
	}
	
}
