package com.cb.driver.kraken;

import com.cb.common.JsonSerializer;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbWriteProvider;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import com.cb.model.kraken.ws.request.KrakenInstrumentSubscriptionRequest;
import com.cb.ws.WebSocketClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

import static com.cb.injection.BindingName.KRAKEN_WEBSOCKET_V2_CLIENT_INSTRUMENT;
import static com.cb.injection.BindingName.KRAKEN_WEBSOCKET_V2_URL;

// TODO: create safety net for this
@Slf4j
public class KrakenInstrumentBridgeDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Kraken Instrument Bridge";

    @Inject
    private DbWriteProvider dbWriteProvider;

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    @Named(KRAKEN_WEBSOCKET_V2_URL)
    private String webSocketUrl;

    @Inject
    @Named(KRAKEN_WEBSOCKET_V2_CLIENT_INSTRUMENT)
    private WebSocketClient webSocketClient;

    public static void main(String[] args) {
        KrakenInstrumentBridgeDriver driver = MainModule.INJECTOR.getInstance(KrakenInstrumentBridgeDriver.class);
        driver.execute();
    }

    @Override
    protected void executeCustom() {
        connect();
        TimeUtils.sleepQuietlyForever();
    }

    @SneakyThrows
    private void connect() {
        KrakenInstrumentSubscriptionRequest subscriptionRequest = new KrakenInstrumentSubscriptionRequest().setReq_id(webSocketClient.getReqId());
        String subscriptionString = jsonSerializer.serializeToJson(subscriptionRequest);
        log.info("WebSocket Subscription: [" + subscriptionString + "]");
        WebSocket webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(webSocketUrl), webSocketClient).join();
        CompletableFuture.runAsync(() -> {
            webSocket.sendText(subscriptionString, true);
            TimeUtils.awaitQuietly(webSocketClient.getLatch());
        });
    }

    @Override
    public String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        dbWriteProvider.cleanup();
    }

}
