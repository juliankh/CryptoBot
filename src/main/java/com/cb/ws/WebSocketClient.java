package com.cb.ws;

import com.cb.processor.BufferAggregator;
import com.cb.processor.JsonProcessor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Getter
@RequiredArgsConstructor
public class WebSocketClient implements WebSocket.Listener {

    private final BufferAggregator bufferAggregator;
    private final JsonProcessor jsonProcessor;
    private final int requestId;

    private final AtomicReference<Instant> latestReceive = new AtomicReference<>();

    @Override
    public void onOpen(WebSocket webSocket) {
        log.info("Opening WebSocket");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        latestReceive.set(Instant.now());
        bufferAggregator.process(data, last, jsonProcessor::process);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error("Error", error);
        cleanup();
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.info("Close: statusCode [" + statusCode + "], reason [" + reason + "]");
        cleanup();
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    private void cleanup() {
        jsonProcessor.cleanup();
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        log.info("Ping: [" + message + "]");
        return WebSocket.Listener.super.onPing(webSocket, message);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        log.info("Pong: [" + message + "]");
        return WebSocket.Listener.super.onPong(webSocket, message);
    }

}
