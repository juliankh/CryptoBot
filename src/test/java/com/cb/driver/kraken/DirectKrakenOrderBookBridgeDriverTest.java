package com.cb.driver.kraken;

import com.cb.alert.Alerter;
import com.cb.common.JsonSerializer;
import com.cb.common.SleepDelegate;
import com.cb.common.util.TimeUtils;
import com.cb.injection.provider.WebSocketClientProvider;
import com.cb.model.CbOrderBook;
import com.cb.processor.kraken.KrakenOrderBookDelegate;
import com.cb.processor.kraken.json.KrakenJsonOrderBookProcessor;
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
import java.time.Month;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.cb.driver.kraken.DirectKrakenOrderBookBridgeDriver.THROTTLE_SLEEP_SECS;
import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private KrakenOrderBookDelegate orderBookDelegate;

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
        Mockito.reset(krakenJsonOrderBookProcessor);
        Mockito.reset(orderBookDelegate);
    }

    @Test
    public void webSocketClosed() {
        assertTrue(driver.webSocketClosed(mockWebSocket(true, true)));
        assertTrue(driver.webSocketClosed(mockWebSocket(true, false)));
        assertTrue(driver.webSocketClosed(mockWebSocket(false, true)));
        assertFalse(driver.webSocketClosed(mockWebSocket(false, false)));
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

        when(orderBookDelegate.orderBookStale(any(Supplier.class), any(Supplier.class), anyInt(), any(Instant.class))).thenReturn(true);

        when(webSocketClient.getJsonProcessor()).thenReturn(krakenJsonOrderBookProcessor);

        Instant timeToCompareTo_DoesNotMatter = Instant.now();

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
        driver.executeIteration(timeToCompareTo_DoesNotMatter);

        // verify
        verify(sleepDelegate, never()).sleepQuietlyForSecs(anyInt());
        String expectedMsg = "Will try to reconnect to WebSocket because latest OrderBook Snapshot is older then " + maxSecsBetweenUpdates + " secs";
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

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNull() {
        // setup
        WebSocketClient webSocketClient = mock(WebSocketClient.class);
        when(webSocketClientProvider.get()).thenReturn(webSocketClient);
        when(webSocketClient.getJsonProcessor()).thenReturn(krakenJsonOrderBookProcessor);
        when(krakenJsonOrderBookProcessor.latestOrderBookSnapshot()).thenReturn(null);
        driver.initializeWebSocketClient();

        // engage test and verify
        assertNull(driver.latestOrderBookExchangeDateTimeSupplier().get());
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNotNull_ExchangeDateTimeNull() {
        // setup
        WebSocketClient webSocketClient = mock(WebSocketClient.class);
        when(webSocketClientProvider.get()).thenReturn(webSocketClient);
        when(webSocketClient.getJsonProcessor()).thenReturn(krakenJsonOrderBookProcessor);
        when(krakenJsonOrderBookProcessor.latestOrderBookSnapshot()).thenReturn(new CbOrderBook().setExchangeDatetime(null));
        driver.initializeWebSocketClient();

        // engage test and verify
        assertNull(driver.latestOrderBookExchangeDateTimeSupplier().get());
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNotNull_ExchangeDateTimeNotNull() {
        // setup
        WebSocketClient webSocketClient = mock(WebSocketClient.class);
        when(webSocketClientProvider.get()).thenReturn(webSocketClient);
        when(webSocketClient.getJsonProcessor()).thenReturn(krakenJsonOrderBookProcessor);
        Instant exchangeDateTime = TimeUtils.instant(1999, Month.MARCH, 27, 10, 37, 15);
        when(krakenJsonOrderBookProcessor.latestOrderBookSnapshot()).thenReturn(new CbOrderBook().setExchangeDatetime(exchangeDateTime));
        driver.initializeWebSocketClient();

        // engage test and verify
        assertEquals(exchangeDateTime, driver.latestOrderBookExchangeDateTimeSupplier().get());
    }

}
