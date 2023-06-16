package com.cb.admin;

import com.cb.alert.Alerter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Deprecated
@ExtendWith(MockitoExtension.class)
public class JmsQueueMonitorTest {

    @Mock
    private Alerter alerter;

    @InjectMocks
    private JmsQueueMonitor jmsQueueMonitor;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(alerter);
    }

    @Test
    public void monitorQueue_messagesBelowLimit() {
        // engage test
        jmsQueueMonitor.monitorQueue("doesn't matter", 4, 5);

        // verify
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorQueue_messagesAtLimit() {
        // engage test
        jmsQueueMonitor.monitorQueue("doesn't matter", 5, 5);

        // verify
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorQueue_messagesAboveLimit() {
        // engage test
        jmsQueueMonitor.monitorQueue("doesn't matter", 6, 5);

        // verify
        verify(alerter, times(1)).sendEmailAlert(anyString(), anyString());
    }

}
