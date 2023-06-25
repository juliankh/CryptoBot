package com.cb.driver.kraken;

import com.cb.alert.Alerter;
import com.cb.common.JsonSerializer;
import com.cb.common.SleepDelegate;
import com.cb.injection.provider.WebSocketClientProvider;
import com.cb.processor.kraken.KrakenJsonOrderBookProcessor;
import com.cb.ws.WebSocketClient;
import com.cb.ws.WebSocketFactory;
import com.cb.ws.WebSocketStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.WebSocket;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.cb.driver.kraken.DirectKrakenOrderBookBridgeDriver.THROTTLE_SLEEP_SECS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DirectKrakenOrderBookBridgeDriverTest {

    @Mock
    private Alerter alerter;

    @Mock
    private JsonSerializer jsonSerializer;

    @Mock
    private WebSocketFactory webSocketFactory;

    @Mock
    private WebSocketClientProvider webSocketClientProvider;

    @Mock
    private SleepDelegate sleepDelegate;

    @Mock
    private CurrencyPair currencyPair;

    @Mock
    private KrakenJsonOrderBookProcessor krakenJsonOrderBookProcessor;

    @InjectMocks
    private DirectKrakenOrderBookBridgeDriver driver;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(alerter);
        Mockito.reset(jsonSerializer);
        Mockito.reset(webSocketFactory);
        Mockito.reset(webSocketClientProvider);
        Mockito.reset(sleepDelegate);
        Mockito.reset(currencyPair);
    }

    @Test
    public void webSocketClosed() {
        assertTrue(driver.webSocketClosed(mockWebSocket(true, true)));
        assertTrue(driver.webSocketClosed(mockWebSocket(true, false)));
        assertTrue(driver.webSocketClosed(mockWebSocket(false, true)));
        assertFalse(driver.webSocketClosed(mockWebSocket(false, false)));
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

    @Test
    public void executeIteration_WebSocketClosed_TryAgainLater_LatestReceiveAgeWithinLimit() {
        // setup
        int maxSecsBetweenUpdates = 25;
        driver.setMaxSecsBetweenUpdates(maxSecsBetweenUpdates);

        String webSocketUrl = "webSocketUrl123";
        driver.setWebSocketUrl(webSocketUrl);

        WebSocket webSocket = mockWebSocket(true, false);
        driver.setWebSocket(webSocket);

        String driverName = "Test Driver";
        driver.setDriverName(driverName);

        WebSocketClient webSocketClient = mock(WebSocketClient.class);
        when(webSocketClientProvider.get()).thenReturn(webSocketClient);

        when(webSocketClient.getJsonProcessor()).thenReturn(krakenJsonOrderBookProcessor);

        Instant timeToCompareTo = Instant.now();
        Instant latestReceive = timeToCompareTo.minusSeconds(maxSecsBetweenUpdates - 1);
        when(webSocketClient.getLatestReceive()).thenReturn(latestReceive);

        int closeStatusCode = WebSocketStatusCode.TRY_AGAIN_LATER;
        when(webSocketClient.getCloseStatusCode()).thenReturn(closeStatusCode);

        String closeReason = "System undergoing maintenance";
        when(webSocketClient.getCloseReason()).thenReturn(closeReason);

        String currencyPairString = "BTC/USDT";
        when(currencyPair.toString()).thenReturn(currencyPairString);

        when(webSocketFactory.webSocket(webSocketUrl, webSocketClient)).thenReturn(webSocket);

        driver.setWebSocketClient(webSocketClient);

        // engage test
        driver.executeIteration(timeToCompareTo);

        // verify
        verify(sleepDelegate, times(1)).sleepQuietlyForSecs(THROTTLE_SLEEP_SECS);
        verify(alerter, times(1)).sendEmailAlertQuietly("Reconn - " + driverName, "Will try to reconnect to WebSocket because WebSocket is closed (Status Code [" + closeStatusCode + "], Reason [" + closeReason + "])");
        verify(webSocket, never()).sendClose(anyInt(), anyString());
        verify(webSocketFactory, times(1)).webSocket(webSocketUrl, webSocketClient);
    }

    @Test
    public void executeIteration_WebSocketClosed_NotTryAgainLater_LatestReceiveAgeWithinLimit() {
        // setup
        int maxSecsBetweenUpdates = 25;
        driver.setMaxSecsBetweenUpdates(maxSecsBetweenUpdates);

        String webSocketUrl = "webSocketUrl123";
        driver.setWebSocketUrl(webSocketUrl);

        WebSocket webSocket = mockWebSocket(true, false);
        driver.setWebSocket(webSocket);

        String driverName = "Test Driver";
        driver.setDriverName(driverName);

        WebSocketClient webSocketClient = mock(WebSocketClient.class);
        when(webSocketClientProvider.get()).thenReturn(webSocketClient);

        when(webSocketClient.getJsonProcessor()).thenReturn(krakenJsonOrderBookProcessor);

        Instant timeToCompareTo = Instant.now();
        Instant latestReceive = timeToCompareTo.minusSeconds(maxSecsBetweenUpdates - 1);
        when(webSocketClient.getLatestReceive()).thenReturn(latestReceive);

        int closeStatusCode = WebSocketStatusCode.SERVER_ERROR;
        when(webSocketClient.getCloseStatusCode()).thenReturn(closeStatusCode);

        String closeReason = "System undergoing maintenance";
        when(webSocketClient.getCloseReason()).thenReturn(closeReason);

        String currencyPairString = "BTC/USDT";
        when(currencyPair.toString()).thenReturn(currencyPairString);

        when(webSocketFactory.webSocket(webSocketUrl, webSocketClient)).thenReturn(webSocket);

        driver.setWebSocketClient(webSocketClient);

        // engage test
        driver.executeIteration(timeToCompareTo);

        // verify
        verify(sleepDelegate, never()).sleepQuietlyForSecs(anyInt());
        verify(alerter, times(1)).sendEmailAlertQuietly("Reconn - " + driverName, "Will try to reconnect to WebSocket because WebSocket is closed (Status Code [" + closeStatusCode + "], Reason [" + closeReason + "])");
        verify(webSocket, never()).sendClose(anyInt(), anyString());
        verify(webSocketFactory, times(1)).webSocket(webSocketUrl, webSocketClient);
    }

    @Test
    public void executeIteration_WebSocketOpen_LatestReceiveAgeOutsideLimit() {
        // setup
        int maxSecsBetweenUpdates = 25;
        driver.setMaxSecsBetweenUpdates(maxSecsBetweenUpdates);

        String webSocketUrl = "webSocketUrl123";
        driver.setWebSocketUrl(webSocketUrl);

        WebSocket webSocket = mockWebSocket(false, false);
        driver.setWebSocket(webSocket);

        String driverName = "Test Driver";
        driver.setDriverName(driverName);

        WebSocketClient webSocketClient = mock(WebSocketClient.class);
        when(webSocketClientProvider.get()).thenReturn(webSocketClient);

        when(webSocketClient.getJsonProcessor()).thenReturn(krakenJsonOrderBookProcessor);

        Instant timeToCompareTo = Instant.now();
        Instant latestReceive = timeToCompareTo.minusSeconds(maxSecsBetweenUpdates + 1);
        when(webSocketClient.getLatestReceive()).thenReturn(latestReceive);
        when(webSocketClient.getCloseStatusCode()).thenReturn(null);
        when(webSocketClient.getCloseReason()).thenReturn(null);

        CompletableFuture<WebSocket> completableFuture = new CompletableFuture<>();
        completableFuture.complete(webSocket);
        when(webSocket.sendClose(anyInt(), anyString())).thenReturn(completableFuture);

        String currencyPairString = "BTC/USDT";
        when(currencyPair.toString()).thenReturn(currencyPairString);

        when(webSocketFactory.webSocket(webSocketUrl, webSocketClient)).thenReturn(webSocket);

        driver.setWebSocketClient(webSocketClient);

        // engage test
        driver.executeIteration(timeToCompareTo);

        // verify
        verify(sleepDelegate, never()).sleepQuietlyForSecs(anyInt());
        String expectedMsg = "Will try to reconnect to WebSocket because no data received in over " + maxSecsBetweenUpdates + " secs";
        verify(alerter, times(1)).sendEmailAlertQuietly("Reconn - " + driverName, expectedMsg);
        verify(webSocket, times(1)).sendClose(WebSocket.NORMAL_CLOSURE, expectedMsg);
        verify(webSocketFactory, times(1)).webSocket(webSocketUrl, webSocketClient);
    }

    @Test
    public void executeIteration_WebSocketOpen_LatestReceiveAgeWithinLimit() {
        // setup
        WebSocketClient webSocketClient = mock(WebSocketClient.class);

        int maxSecsBetweenUpdates = 25;
        driver.setMaxSecsBetweenUpdates(maxSecsBetweenUpdates);
        WebSocket webSocket = mockWebSocket(false, false);
        driver.setWebSocket(webSocket);
        Instant timeToCompareTo = Instant.now();
        Instant latestReceive = timeToCompareTo.minusSeconds(maxSecsBetweenUpdates - 1);
        when(webSocketClient.getLatestReceive()).thenReturn(latestReceive);

        driver.setWebSocketClient(webSocketClient);

        // engage test
        driver.executeIteration(timeToCompareTo);

        // verify
        verify(sleepDelegate, never()).sleepQuietlyForSecs(anyInt());
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
        verify(webSocket, never()).sendClose(anyInt(), anyString());
        verify(webSocketFactory, never()).webSocket(anyString(), any(WebSocket.Listener.class));
    }

    private WebSocket mockWebSocket(boolean inputClosed, boolean outputClosed) {
        WebSocket websocket = mock(WebSocket.class);
        when(websocket.isInputClosed()).thenReturn(inputClosed);
        when(websocket.isOutputClosed()).thenReturn(outputClosed);
        return websocket;
    }

}
