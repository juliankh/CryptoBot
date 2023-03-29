package com.cb.alert;

public interface AlertProvider {
	
	void sendTextAlert(String msg);
	void sendEmailAlert(String subject, String body, Throwable t);
	void sendEmailAlert(String subject, String body);
	void sendAlert(String subject, String body, String recipient);
	
}
