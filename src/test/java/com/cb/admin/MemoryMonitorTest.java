package com.cb.admin;

import com.cb.alert.Alerter;
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
public class MemoryMonitorTest {

    @Mock
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Mock
    private Alerter alerter;

    @InjectMocks
    private MemoryMonitor memoryMonitor;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(dbReadOnlyProvider);
        Mockito.reset(alerter);
    }

    @Test
    public void alertIfAboveLimit_aboveThreshold() {
        memoryMonitor.alertIfAboveLimit(0.26, 25);
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void alertIfAboveLimit_belowThreshold() {
        memoryMonitor.alertIfAboveLimit(0.24, 25);
        verify(alerter, times(1)).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void ratio() {
        assertEquals(0.25, memoryMonitor.ratio(25, 100), DOUBLE_COMPARE_DELTA);
    }

}
