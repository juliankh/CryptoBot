package com.cb.processor.kraken;

import com.cb.model.kraken.ws.response.KrakenError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class KrakenAbstractJsonProcessorTest {

    private static final int REQUEST_ID = 102938;
    private static final int REQUEST_ID_DIFFERENT = 876543345;

    @InjectMocks
    private KrakenJsonOrderBookProcessor processor; // using orderbook processor because can't instantiate KrakenAbstractJsonProcessor which is an abstract class

    @BeforeEach
    public void beforeEachTest() {
        processor.initialize(REQUEST_ID);
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

}
