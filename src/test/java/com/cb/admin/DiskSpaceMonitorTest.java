package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.cb.test.CryptoBotTestUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DiskSpaceMonitorTest {

    @Mock
    private DbProvider dbProvider;

    @Mock
    private AlertProvider alertProvider;

    @InjectMocks
    private DiskSpaceMonitor diskSpaceMonitor;

    @Before
    public void beforeEachTest() {
        Mockito.reset(dbProvider);
        Mockito.reset(alertProvider);
    }

    @Test
    public void alertIfAboveLimit_aboveThreshold() {
        diskSpaceMonitor.alertIfAboveLimit(0.26, 25);
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void alertIfAboveLimit_belowThreshold() {
        diskSpaceMonitor.alertIfAboveLimit(0.24, 25);
        verify(alertProvider, times(1)).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void usableRatio() {
        assertEquals(0.25, diskSpaceMonitor.usableRatio(100, 25), DOUBLE_COMPARE_DELTA);
        assertEquals(4.0, diskSpaceMonitor.usableRatio(25, 100), DOUBLE_COMPARE_DELTA);
    }

}
