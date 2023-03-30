package com.cb.driver;

import com.cb.alert.AlertProvider;
import com.cb.util.CryptoUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public abstract class AbstractDriver {
	protected AlertProvider alertProvider;

	public abstract String getDriverName();

	protected abstract void executeCustom() throws Exception;

	public AbstractDriver(AlertProvider alertProvider) {
		this.alertProvider = alertProvider;
	}
	public void execute() {
		Instant startTime = Instant.now();
		try {
			log.info("Started [" + getDriverName()  + "] process");
			executeCustom();
		} catch (Throwable e) {
			String errMsg = "Problem executing [" + getDriverName() + "] process";
			log.error(errMsg, e);
			alertProvider.sendEmailAlert(errMsg, errMsg, e);						
			logProcessDuration(startTime);
			throw new RuntimeException(errMsg, e);
		}
		logProcessDuration(startTime);
	}

	protected void logProcessDuration(Instant startTime) {
		log.info("Process [" + getDriverName() + "] took " + CryptoUtils.durationMessage(startTime));
	}
		
}
