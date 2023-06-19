package com.cb.processor.kraken;

import com.cb.alert.Alerter;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseInstrument;
import com.cb.ws.kraken.KrakenJsonInstrumentObjectConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KrakenJsonInstrumentProcessorTest {

    private static final String DRIVER_NAME = "driver name 123";
    private static final int REQUEST_ID = 102938;
    private static final int REQUEST_ID_DIFFERENT = 876543345;

    @Mock
    private Alerter alerter;

    @Mock
    private KrakenJsonInstrumentObjectConverter jsonObjectConverter;

    @InjectMocks
    private KrakenJsonInstrumentProcessor processor;

    @BeforeEach
    public void beforeEachTest() {
        reset(alerter);
        reset(jsonObjectConverter);
        processor.initialize(DRIVER_NAME, REQUEST_ID);
    }

    @Test
    public void processSubscriptionResponse_Successful_RequestIdMatches() {
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(subscriptionResponse(true, REQUEST_ID)));
    }

    @Test
    public void processSubscriptionResponse_Successful_RequestIdDoesNotMatch() {
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(subscriptionResponse(true, REQUEST_ID_DIFFERENT)));
    }

    @Test
    public void processSubscriptionResponse_Unsuccessful_RequestIdMatches() {
        KrakenSubscriptionResponseInstrument unsuccessfulResponse = subscriptionResponse(false, REQUEST_ID);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processSubscriptionResponse(unsuccessfulResponse));
        assertEquals("Error when trying to subscribe to Kraken Instrument channel: " + unsuccessfulResponse, exception.getMessage());
    }

    @Test
    public void processSubscriptionResponse_Unsuccessful_RequestIdDoesNotMatch() {
        KrakenSubscriptionResponseInstrument unsuccessfulResponse = subscriptionResponse(false, REQUEST_ID_DIFFERENT);
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(unsuccessfulResponse));
    }

    private static KrakenSubscriptionResponseInstrument subscriptionResponse(boolean successful, int requestId) {
        KrakenSubscriptionResponseInstrument response = new KrakenSubscriptionResponseInstrument();
        response.setSuccess(successful);
        response.setReq_id(requestId);
        return response;
    }

    @Test
    public void processCustom_UnknownObjectTypeParsed() {
        // setup
        Class<?> unknownClass = BigDecimal.class;

        // engage test and verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processCustom(unknownClass));
        assertEquals("Unknown object type parsed: [" + unknownClass + "]", exception.getMessage());
    }

    @Test
    public void process_ExceptionThrown() {
        // setup
        String json = "some json that can't be parsed";
        NullPointerException exception = new NullPointerException();
        doThrow(exception).when(jsonObjectConverter).parse(json);

        // engage test
        processor.process(json);

        // verify
        verify(alerter, times(1)).sendEmailAlertQuietly(DRIVER_NAME + ": Problem w/json", json, exception);
    }

}
