package com.cb.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

public class WebSocketFactory {

    public WebSocket webSocket(String webSocketUrl, WebSocket.Listener webSocketListener) {
        return HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(webSocketUrl), webSocketListener).join();
    }

}
