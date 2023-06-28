package com.cb.driver.kraken;

import com.cb.common.JsonSerializer;
import com.cb.common.util.TimeUtils;
import com.cb.db.WriteDao;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import com.cb.injection.provider.KrakenInstrumentWebSocketClientProvider;
import com.cb.model.kraken.ws.request.KrakenInstrumentSubscriptionRequest;
import com.cb.processor.kraken.json.KrakenJsonInstrumentProcessor;
import com.cb.ws.WebSocketClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

import static com.cb.injection.BindingName.KRAKEN_WEBSOCKET_V2_URL;

@Slf4j
public class DirectKrakenInstrumentBridgeDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Kraken Instrument Bridge";

    @Inject
    private WriteDao writeDao;

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    @Named(KRAKEN_WEBSOCKET_V2_URL)
    private String webSocketUrl;

    @Inject
    private KrakenInstrumentWebSocketClientProvider krakenInstrumentWebSocketClientProvider;

    private WebSocketClient webSocketClient;

    public static void main(String[] args) {
        DirectKrakenInstrumentBridgeDriver driver = MainModule.INJECTOR.getInstance(DirectKrakenInstrumentBridgeDriver.class);
        driver.initialize();
        driver.execute();
    }

    public void initialize() {
        log.info("Initializing WebSocketClient");
        webSocketClient = krakenInstrumentWebSocketClientProvider.get();
        ((KrakenJsonInstrumentProcessor)webSocketClient.getJsonProcessor()).initialize(getDriverName(), webSocketClient.getRequestId());
    }

    @Override
    protected void executeCustom() {
        connect();
        TimeUtils.sleepQuietlyForSecs(20);
    }

    @SneakyThrows
    private void connect() {
        KrakenInstrumentSubscriptionRequest subscriptionRequest = new KrakenInstrumentSubscriptionRequest().setReq_id(webSocketClient.getRequestId());
        String subscriptionString = jsonSerializer.serializeToJson(subscriptionRequest);
        log.info("WebSocket Subscription: [" + subscriptionString + "]");
        WebSocket webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(webSocketUrl), webSocketClient).join();
        webSocket.sendText(subscriptionString, true);
    }

    @Override
    public String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        writeDao.cleanup();
    }

}
