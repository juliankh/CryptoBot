package com.cb.ws;

import com.cb.common.BufferAggregator;
import com.cb.processor.JsonProcessor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

@Slf4j
@Getter
@RequiredArgsConstructor
public class WebSocketClient implements WebSocket.Listener {

    private final BufferAggregator bufferAggregator;
    private final JsonProcessor jsonProcessor;
    private final int requestId;

    private Instant latestReceive; // TODO: if decide to proceed with direct bridges and discard the xchange bridges, then remove this field
    private Integer closeStatusCode;
    private String closeReason;

    @Override
    public void onOpen(WebSocket webSocket) {
        log.info("Opening WebSocket");
        reset();
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public synchronized CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        latestReceive = Instant.now();
        bufferAggregator.process(data, last, jsonProcessor::process);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error("Error", error);
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.info("Close: statusCode [" + statusCode + "], reason [" + reason + "]");
        this.closeStatusCode = statusCode;
        this.closeReason = reason;
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    public void reset() {
        bufferAggregator.reset();
    }

    public void cleanup() {
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
