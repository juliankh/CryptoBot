package com.cb.driver;

import com.cb.alert.AlertProvider;
import com.cb.common.util.TimeUtils;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public abstract class AbstractDriver {

	@Inject
	protected AlertProvider alertProvider;

	protected abstract String getDriverName();
	protected abstract void executeCustom();
	protected abstract void cleanup();

	public void execute() {
		Instant startTime = Instant.now();
		try {
			log.info("Started [" + getDriverName()  + "] process");
			executeCustom();
		} catch (Throwable e) {
			String errMsg = "Problem executing [" + getDriverName() + "] process";
			log.error(errMsg, e);
			logProcessDuration(startTime);
			cleanup();
			alertProvider.sendEmailAlertQuietly(errMsg, errMsg, e);
			throw e;
		}
		logProcessDuration(startTime);
	}

	protected void logProcessDuration(Instant startTime) {
		log.info("Process [" + getDriverName() + "] took " + TimeUtils.durationMessage(startTime));
	}
		
}
