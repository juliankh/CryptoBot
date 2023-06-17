package com.cb.alert;

import com.cb.common.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class AlertDelegate {

    public Session session(Properties emailProperties, String alertEmail, String alertPassword) {
        return Session.getDefaultInstance(emailProperties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(alertEmail, alertPassword);
                    }
                });
    }

    public void send(Session session, String from, String to, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }

    public void handleTooManyLoginAttempts(Alerter alerter, int throttleSleepMins) {
        CompletableFuture.runAsync(() -> {
            log.info("Attempt to send alert is being throttled, so will temporarily turn OFF alerting for [" + throttleSleepMins + "] mins");
            alerter.setAlertingOn(false);
            TimeUtils.sleepQuietlyForMins(throttleSleepMins);
            alerter.setAlertingOn(true);
            log.info("Alerting is back ON");
        });
    }

}
