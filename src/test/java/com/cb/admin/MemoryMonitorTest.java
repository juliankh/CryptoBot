package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbReadOnlyProvider;
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
public class MemoryMonitorTest {

    @Mock
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Mock
    private AlertProvider alertProvider;

    @InjectMocks
    private MemoryMonitor memoryMonitor;

    @Before
    public void beforeEachTest() {
        Mockito.reset(dbReadOnlyProvider);
        Mockito.reset(alertProvider);
    }

    @Test
    public void alertIfAboveLimit_aboveThreshold() {
        memoryMonitor.alertIfAboveLimit(0.26, 25);
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void alertIfAboveLimit_belowThreshold() {
        memoryMonitor.alertIfAboveLimit(0.24, 25);
        verify(alertProvider, times(1)).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void ratio() {
        assertEquals(0.25, memoryMonitor.ratio(25, 100), DOUBLE_COMPARE_DELTA);
    }

}
