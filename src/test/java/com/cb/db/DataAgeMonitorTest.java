package com.cb.db;

import com.cb.alert.AlertProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataAgeMonitorTest {

    @Mock
    private AlertProvider alertProvider;

    @InjectMocks
    private DataAgeMonitor dataAgeMonitor;

    @Before
    public void beforeEachTest() {
        Mockito.reset(alertProvider);
    }

    @Test
    public void monitorTable_ageBelowLimit() {
        // prepare data
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);

        // engage test
        dataAgeMonitor.monitorTable("doesn't matter", timeOfLastItem, timeToCompare, 6);

        // verify
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorTable_ageAtLimit() {
        // prepare data
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);

        // engage test
        dataAgeMonitor.monitorTable("doesn't matter", timeOfLastItem, timeToCompare, 5);

        // verify
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorTable_ageAboveLimit() {
        // prepare data
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);

        // engage test
        dataAgeMonitor.monitorTable("doesn't matter", timeOfLastItem, timeToCompare, 4);

        // verify
        verify(alertProvider, times(1)).sendEmailAlert(anyString(), anyString());
    }

}
