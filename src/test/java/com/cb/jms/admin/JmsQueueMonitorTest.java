package com.cb.jms.admin;

import com.cb.alert.AlertProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JmsQueueMonitorTest {

    @Mock
    private AlertProvider alertProvider;

    @InjectMocks
    private JmsQueueMonitor jmsQueueMonitor;

    @Before
    public void beforeEachTest() {
        Mockito.reset(alertProvider);
    }

    @Test
    public void monitorQueue_messagesBelowLimit() {
        // engage test
        jmsQueueMonitor.monitorQueue("doesn't matter", 4, 5);

        // verify
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorQueue_messagesAtLimit() {
        // engage test
        jmsQueueMonitor.monitorQueue("doesn't matter", 5, 5);

        // verify
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorQueue_messagesAboveLimit() {
        // engage test
        jmsQueueMonitor.monitorQueue("doesn't matter", 6, 5);

        // verify
        verify(alertProvider, times(1)).sendEmailAlert(anyString(), anyString());
    }

}
