package com.cb.ws.kraken;

import com.cb.common.util.TimeUtils;
import com.cb.ws.WebSocketClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: delete this class
@Slf4j
public class App_Works3 {

    public static void main(String[] args) throws Exception {
        int numParallelConnections = 10;
        //List<String> pairs = Lists.newArrayList("BTC/USDT", "SOL/USD", "ATOM/USD", "LINK/USD", "CHR/USD");
        //List<String> pairs = Lists.newArrayList("BTC/USDT", "LINK/USD", "CHR/USD");
        //log.info("Num of parallel websocket connections per pair: " + numParallelConnections);
        //pairs.parallelStream().forEach(pair -> {
            //List<Pair<Integer, Integer>> results = IntStream.range(0, numParallelConnections).parallel().mapToObj(connectionNum -> Pair.of(connectionNum, doIt(connectionNum, 500))).toList();
            //IntStream.range(0, numParallelConnections).parallel().forEach(connectionNum -> doIt(pair, connectionNum + 1, 500));
        //});
        //log.info("Results:\n" + results.stream().map(Object::toString).collect(Collectors.joining("\n")));
        doIt("XRP/USD", 10, 10);
    }

    public static int doIt(String pair, int connectionNum, int depth) {
        //String publicWebSocketURL = "wss://ws.kraken.com/";
        String publicWebSocketURL = "wss://ws.kraken.com/v2";

        //String publicWebSocketSubscriptionMsg = "{ \"event\":\"subscribe\", \"subscription\":{\"name\":\"book\",\"depth\":" + depth + "},\"pair\":[\"" + pair + "\"] }";
        String publicWebSocketSubscriptionMsg = "{\n" +
                "  \"method\": \"subscribe\",\n" +
                "  \"params\": {\n" +
                "    \"channel\": \"book\",\n" +
                "    \"depth\": " + depth + ",\n" +
                "    \"snapshot\": true,\n" +
                "    \"symbol\": [\n" +
                "      \"" + pair + "\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"req_id\": 1234567890\n" +
                "}";

        log.info("Connection [" + pair + " " + connectionNum + "]: " + publicWebSocketSubscriptionMsg);
        /*
         * MORE PUBLIC WEBSOCKET EXAMPLES
         *
         * String publicWebSocketSubscriptionMsg = "{ \"event\": \"subscribe\", \"subscription\": { \"interval\": 1440, \"name\": \"ohlc\"}, \"pair\": [ \"XBT/EUR\" ]}";
         * String publicWebSocketSubscriptionMsg = "{ \"event\": \"subscribe\", \"subscription\": { \"name\": \"spread\"}, \"pair\": [ \"XBT/EUR\",\"ETH/USD\" ]}";
         */
        return OpenAndStreamWebSocketSubscription(publicWebSocketURL, publicWebSocketSubscriptionMsg, pair, connectionNum, depth);
    }

    /*
     * WebSocket API
     */
    public static int OpenAndStreamWebSocketSubscription(String connectionURL, String webSocketSubscription, String pair, int connectionNum, int depth) {
        AtomicInteger totalCount = new AtomicInteger();
        //IntStream.range(0, 10).forEach(i -> {
            totalCount.addAndGet(connectWebSocket(connectionURL, webSocketSubscription, pair, connectionNum, depth));
        //});
        return totalCount.get();
    }

    @SneakyThrows
    private static int connectWebSocket(String connectionURL, String webSocketSubscription, String pair, int connectionNum, int depth) {
        WebSocketClient client = new WebSocketClient();
        WebSocket ws = HttpClient
            .newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(URI.create(connectionURL), client)
            .join();
        ws.sendText(webSocketSubscription, true);
        //TimeUtils.sleepQuietlyForMins(10);
        TimeUtils.sleepQuietlyForMins(999_999);
        return -1;
    }

}
