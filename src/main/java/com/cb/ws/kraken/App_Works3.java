package com.cb.ws.kraken;

import com.cb.common.ObjectConverter;
import com.cb.common.util.TimeUtils;
import com.cb.ws.WebSocketClient;
import com.cb.ws.kraken.request.OrderBookSubscription;
import com.cb.ws.kraken.request.OrderBookSubscriptionParams;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

// TODO: delete this class
@Slf4j
public class App_Works3 {

    public static void main(String[] args) throws Exception {
        OrderBookSubscription subscription = new OrderBookSubscription()
                .setReq_id(2746)
                .setParams(new OrderBookSubscriptionParams().setSnapshot(true).setDepth(100).setSymbol(Lists.newArrayList(CurrencyPair.BTC_USDT.toString())));
        String subscriptionString = ObjectConverter.OBJECT_MAPPER.writer().writeValueAsString(subscription);
        WebSocketClient client = new WebSocketClient(null, null);
        WebSocket webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(""), client).join();

        CompletableFuture.runAsync(() -> {
            webSocket.sendText(subscriptionString, true);
            TimeUtils.awaitQuietly(client.getLatch());
        });

        TimeUtils.sleepQuietlyForSecs(10);
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "some problem");
    }

}
