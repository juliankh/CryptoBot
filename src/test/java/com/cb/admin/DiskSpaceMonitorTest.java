package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbReadOnlyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.cb.common.util.NumberUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiskSpaceMonitorTest {

    @Mock
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Mock
    private AlertProvider alertProvider;

    @InjectMocks
    private DiskSpaceMonitor diskSpaceMonitor;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(dbReadOnlyProvider);
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
