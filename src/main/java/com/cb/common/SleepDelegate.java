package com.cb.common;

import com.cb.common.util.TimeUtils;

public class SleepDelegate {

    public void sleepQuietlyForSecs(int secs) {
        TimeUtils.sleepQuietlyForMillis(secs);
    }

    public void sleepQuietlyForMins(int mins) {
        TimeUtils.sleepQuietlyForMillis(mins);
    }

    public void sleepQuietlyForever() {
        TimeUtils.sleepQuietlyForever();
    }

    public void sleepQuietlyForMillis(long millis) {
        TimeUtils.sleepQuietlyForMillis(millis);
    }

}
