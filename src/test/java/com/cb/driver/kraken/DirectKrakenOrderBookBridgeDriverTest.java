package com.cb.driver.kraken;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.WebSocket;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DirectKrakenOrderBookBridgeDriverTest {

    @InjectMocks
    private DirectKrakenOrderBookBridgeDriver driver;

    @Test
    public void webSocketClosed() {
        assertTrue(driver.webSocketClosed(mockWebSocket(true, true)));
        assertTrue(driver.webSocketClosed(mockWebSocket(true, false)));
        assertTrue(driver.webSocketClosed(mockWebSocket(false, true)));
        assertFalse(driver.webSocketClosed(mockWebSocket(false, false)));
    }

    private WebSocket mockWebSocket(boolean inputClosed, boolean outputClosed) {
        WebSocket websocket = mock(WebSocket.class);
        when(websocket.isInputClosed()).thenReturn(inputClosed);
        when(websocket.isOutputClosed()).thenReturn(outputClosed);
        return websocket;
    }

    @Test
    public void latestReceiveAgeOverLimit() {
        int maxSecsBetweenUpdates = 60;
        Instant timeToCompareTo = Instant.now();

        assertFalse(driver.latestReceiveAgeOverLimit(null, timeToCompareTo, maxSecsBetweenUpdates));
        assertFalse(driver.latestReceiveAgeOverLimit(timeToCompareTo.minusSeconds(maxSecsBetweenUpdates - 1), timeToCompareTo, maxSecsBetweenUpdates));
        assertFalse(driver.latestReceiveAgeOverLimit(timeToCompareTo.minusSeconds(maxSecsBetweenUpdates), timeToCompareTo, maxSecsBetweenUpdates));
        assertTrue(driver.latestReceiveAgeOverLimit(timeToCompareTo.minusSeconds(maxSecsBetweenUpdates + 1), timeToCompareTo, maxSecsBetweenUpdates));
    }

}
