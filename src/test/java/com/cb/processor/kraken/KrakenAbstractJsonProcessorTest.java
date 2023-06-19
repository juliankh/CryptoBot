package com.cb.processor.kraken;

import com.cb.alert.Alerter;
import com.cb.exception.ChecksumException;
import com.cb.model.kraken.ws.response.KrakenError;
import com.cb.ws.kraken.KrakenJsonOrderBookObjectConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KrakenAbstractJsonProcessorTest {

    private static final String DRIVER_NAME = "driver name 345";
    private static final int REQUEST_ID = 102938;
    private static final int REQUEST_ID_DIFFERENT = 876543345;

    @Mock
    private Alerter alerter;

    @Mock
    private KrakenJsonOrderBookObjectConverter jsonObjectConverter;

    @InjectMocks
    private KrakenJsonOrderBookProcessor processor; // using orderbook processor because can't instantiate KrakenAbstractJsonProcessor which is an abstract class

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(alerter);
        Mockito.reset(jsonObjectConverter);
        processor.initialize(DRIVER_NAME, REQUEST_ID);
    }

    @Test
    public void processError_requestIdMatches() {
        // setup
        KrakenError error = new KrakenError();
        error.setReq_id(REQUEST_ID);

        // engage test and verify
        assertDoesNotThrow(() -> processor.processError(error));
    }

    @Test
    public void processError_requestIdDoesNotMatch() {
        // setup
        KrakenError error = new KrakenError();
        error.setReq_id(REQUEST_ID_DIFFERENT);

        // engage test and verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processError(error));
        assertEquals("Got error from Kraken: " + error, exception.getMessage());
    }

    @Test
    public void process_ChecksumException() {
        // setup
        String json = "some json";
        String checksumExceptionMsg = "some problem related to checksum";
        when(jsonObjectConverter.objectTypeParsed()).thenThrow(new ChecksumException(checksumExceptionMsg));

        // engage test and verify
        ChecksumException result = assertThrows(ChecksumException.class, () -> processor.process(json));
        assertEquals(checksumExceptionMsg, result.getMessage());
        verify(alerter, never()).sendEmailAlertQuietly(anyString(), anyString(), any(Exception.class));
    }

    @Test
    public void process_NonChecksumException() {
        // setup
        String json = "some json";
        NullPointerException exception = new NullPointerException();
        when(jsonObjectConverter.objectTypeParsed()).thenThrow(exception);

        // engage test and verify
        assertDoesNotThrow(() -> processor.process(json));
        verify(alerter, times(1)).sendEmailAlertQuietly(DRIVER_NAME + ": Problem w/json", json, exception);
    }

}
