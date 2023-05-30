package com.cb.ws;

import com.cb.processor.BufferProcessor;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

@Slf4j
@Getter
public class WebSocketClient implements WebSocket.Listener {

    @Inject
    private BufferProcessor bufferProcessor;

    @Override
    public void onOpen(WebSocket webSocket) {
        log.info("onOpen");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        System.out.println(data); // TODO: remove
        /*
        ++counter;
        log.info(counter + " (depth " + depth + ") onText (" + last + "): " + data);
        if (counter % 1000 == 0) {
            log.info("[" + pair + " " + connectionNum + "] - counter: " + counter);
        }
        if (!last) {
            sb.append(data);
        } else {
            sb.setLength(0);
        }*/
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error("Error", error);
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        log.error("Ping: [" + message + "]");
        return WebSocket.Listener.super.onPing(webSocket, message);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        log.error("Pong: [" + message + "]");
        return WebSocket.Listener.super.onPong(webSocket, message);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.error("Close: statusCode [" + statusCode + "], reason [" + reason + "]");
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

}
