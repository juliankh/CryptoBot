package com.cb.alert;

import com.cb.common.GeneralDelegate;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlerterTest {

    @Mock
    private AlertDelegate alertDelegate;

    @Mock
    private GeneralDelegate generalDelegate;

    @InjectMocks
    private Alerter alerter;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(alertDelegate);
        Mockito.reset(generalDelegate);
    }

    @SneakyThrows
    @Test
    public void sendAlert_alertingOff() {
        // setup
        alerter.setAlertingOn(false);
        String hostname_doesNotMatter = "hostname123";
        when(generalDelegate.hostname(anyInt())).thenReturn(hostname_doesNotMatter);
        Properties emailProperties_doesNotMatter = new Properties();

        // engage test
        alerter.sendAlert(emailProperties_doesNotMatter, "subject123", "body123", "recipient123", false);

        // verify
        verify(alertDelegate, never()).session(any(Properties.class), anyString(), anyString());
        verify(alertDelegate, never()).send(any(Session.class), anyString(), anyString(), anyString(), anyString());
    }

    @SneakyThrows
    @Test
    public void sendAlert_alertingOn() {
        // setup
        alerter.setAlertingOn(true);

        String alertEmail = "alertEmail123";
        String alertPassword = "alertPassword123";
        alerter.setAlertEmail(alertEmail);
        alerter.setAlertPassword(alertPassword);

        String hostname_doesNotMatter = "hostname123";
        when(generalDelegate.hostname(anyInt())).thenReturn(hostname_doesNotMatter);

        Session session = mock(Session.class);
        when(alertDelegate.session(any(Properties.class), anyString(), anyString())).thenReturn(session);

        Properties emailProperties_doesNotMatter = new Properties();

        // engage test
        alerter.sendAlert(emailProperties_doesNotMatter, "subject123", "body123", "recipient123", false);

        // verify
        verify(alertDelegate, times(1)).session(emailProperties_doesNotMatter, alertEmail, alertPassword);
        verify(alertDelegate, times(1)).send(any(Session.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void handle_InnerMessagingException_TooManyLoginAttempts() {
        // setup
        MessagingException exception = new MessagingException("Way too many login attempts took place");
        String subject_doesNotMatter = "subject doesn't matter";

        // engage test and verify
        RuntimeException result = assertThrows(RuntimeException.class, () -> alerter.handle(exception, subject_doesNotMatter));
        assertEquals("Problem sending alert with subject [" + subject_doesNotMatter + "]", result.getMessage());
        verify(alertDelegate, times(1)).handleTooManyLoginAttempts(alerter, Alerter.THROTTLE_SLEEP_MINS);
    }

    @Test
    public void handle_InnerMessagingException_NotTooManyLoginAttempts() {
        // setup
        MessagingException exception = new MessagingException("Logging attempts not too many, only a few");
        String subject_doesNotMatter = "subject doesn't matter";

        // engage test and verify
        RuntimeException result = assertThrows(RuntimeException.class, () -> alerter.handle(exception, subject_doesNotMatter));
        assertEquals("Problem sending alert with subject [" + subject_doesNotMatter + "]", result.getMessage());
        verify(alertDelegate, never()).handleTooManyLoginAttempts(any(Alerter.class), anyInt());
    }

    @Test
    public void handle_OuterException() {
        // setup
        String exceptionMessage = "exception message 123";
        String subject = "subject 123";

        // engage test and verify when shouldn't throw exception
        assertDoesNotThrow(() -> alerter.handle(new Exception(exceptionMessage), subject, true));

        // engage test and verify when should throw exception
        IllegalStateException result = assertThrows(IllegalStateException.class, () -> alerter.handle(new IllegalStateException(exceptionMessage), subject, false));
        assertEquals(exceptionMessage, result.getMessage());
    }

}
