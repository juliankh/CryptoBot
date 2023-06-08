package com.cb.processor.kraken;

import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseInstrument;
import com.cb.ws.kraken.json_converter.KrakenJsonInstrumentObjectConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
public class KrakenJsonInstrumentProcessorTest {

    @Mock
    private KrakenJsonInstrumentObjectConverter jsonObjectConverter;

    @InjectMocks
    private KrakenJsonInstrumentProcessor processor;

    @BeforeEach
    public void beforeEachTest() {
        reset(jsonObjectConverter);
    }

    @Test
    public void processSubscriptionResponse_Successful() {
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(subscriptionResponse(true)));
    }

    @Test
    public void processSubscriptionResponse_Unsuccessful() {
        KrakenSubscriptionResponseInstrument unsuccessfulResponse = subscriptionResponse(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processSubscriptionResponse(unsuccessfulResponse));
        assertEquals("Error when trying to subscribe to Kraken Instrument channel: " + unsuccessfulResponse, exception.getMessage());
    }

    private static KrakenSubscriptionResponseInstrument subscriptionResponse(boolean successful) {
        KrakenSubscriptionResponseInstrument response = new KrakenSubscriptionResponseInstrument();
        response.setSuccess(successful);
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
        String json = "some json";
        doThrow(NullPointerException.class).when(jsonObjectConverter).parse(json);

        // engage test and verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.process(json));
        assertEquals("Problem processing json: [" + json + "]", exception.getMessage());
    }

}
