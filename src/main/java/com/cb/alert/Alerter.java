package com.cb.alert;

import com.cb.common.GeneralDelegate;
import com.cb.injection.module.MainModule;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.IOException;
import java.util.Properties;

import static com.cb.injection.BindingName.*;

@Slf4j
@Setter
@Singleton
public class Alerter {

	public static final int THROTTLE_SLEEP_MINS = 3;

	@Inject
	@Named(ALERT_EMAIL)
	private String alertEmail;

	@Inject
	@Named(ALERT_PASSWORD)
	private String alertPassword;

	@Inject
	@Named(ALERT_TEXT_NUM)
	private String alertTextNum;

	@Inject
	@Named(ALERT_SMTP_HOST)
	private String alertSmtpHost;

	@Inject
	@Named(ALERT_SMTP_SOCKET_FACTORY_PORT)
	private String alertSmtpSocketFactoryPort;

	@Inject
	@Named(ALERT_SMTP_SOCKET_FACTORY_CLASS)
	private String alertSmtpSocketFactoryClass;

	@Inject
	@Named(ALERT_SMTP_AUTH)
	private String alertSmtpAuth;

	@Inject
	@Named(ALERT_SMTP_PORT)
	private String alertSmtpPort;

	@Inject
	private AlertDelegate alertDelegate;

	@Inject
	private GeneralDelegate generalDelegate;

	public boolean alertingOn = true;

	/*
	// for manual testing
	public static void main(String[] args) throws IOException {
		AlertProvider alertProvider = MainModule.INJECTOR.getInstance(AlertProvider.class);
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
		(alertProvider).sendEmailAlert("subj", "bodbod", t);
		//(new AlertProviderImpl()).sendTextAlert("hey hello");
	}*/

	// DO NOT MODIFY/DELETE -- this is used by safety net driver wrapper scripts
	public static void main(String[] args) throws IOException {
		Alerter alerter = MainModule.INJECTOR.getInstance(Alerter.class);
		alerter.sendEmailAlert(args[0], args[1]);
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
		sendAlert(subject, body, alertEmail, quietly);
	}

	public void sendTextAlert(String msg) {
		sendTextAlert(msg, false);
	}

	public void sendTextAlertQuietly(String msg) {
		sendTextAlert(msg, true);
	}

	public void sendTextAlert(String msg, boolean quietly) {
		sendAlert(msg, msg, alertTextNum, quietly);
	}

	public void sendAlert(String subject, String body, String recipient, boolean quietly) {
		sendAlert(emailProperties(), subject, body, recipient, quietly);
	}

	public synchronized void sendAlert(Properties emailProperties, String subject, String body, String recipient, boolean quietly) {
		try {
			String hostnamePrefix = generalDelegate.hostname(5);
			subject = hostnamePrefix + ": " + subject;
			if (!alertingOn) {
				log.info("NOT Sending alert with subject [" + subject + "] because the Alert system is OFF");
				return;
			}
			log.info("Sending alert with subject [" + subject + "]");
			Session session = alertDelegate.session(emailProperties, alertEmail, alertPassword);
			try {
				alertDelegate.send(session, alertEmail, recipient, subject, body);
			} catch (MessagingException e) {
				handle(e, subject);
			}
		} catch (Exception e) {
			handle(e, subject, quietly);
		}
	}

	public void handle(MessagingException e, String subject) {
		log.error("Problem sending alert", e);
		if (StringUtils.containsIgnoreCase(e.getMessage(), "Too many login attempts")) {
			alertDelegate.handleTooManyLoginAttempts(this, THROTTLE_SLEEP_MINS);
		}
		throw new RuntimeException("Problem sending alert with subject [" + subject + "]", e);
	}

	@SneakyThrows
	public void handle(Exception e, String subject, boolean quietly) {
		if (quietly) {
			log.error("Problem sending alert with subject [" + subject + "].  Logging, but otherwise ignoring because the quietly flag is ON.", e);
		} else {
			throw e;
		}
	}

	private Properties emailProperties() {
		Properties emailProperties = new Properties();
		emailProperties.put("mail.smtp.host", alertSmtpHost);
		emailProperties.put("mail.smtp.socketFactory.port", alertSmtpSocketFactoryPort);
		emailProperties.put("mail.smtp.socketFactory.class", alertSmtpSocketFactoryClass);
		emailProperties.put("mail.smtp.auth", alertSmtpAuth);
		emailProperties.put("mail.smtp.port", alertSmtpPort);
		return emailProperties;
	}
	
}
