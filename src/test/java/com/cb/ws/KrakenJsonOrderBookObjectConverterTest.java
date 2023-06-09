package com.cb.ws;

import com.cb.common.JsonSerializer;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBook2Data;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookInfo;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookLevel;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseOrderBook;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseOrderBookResult;
import com.cb.ws.kraken.json_converter.KrakenJsonOrderBookObjectConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static com.cb.common.util.NumberUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class KrakenJsonOrderBookObjectConverterTest {

    @Spy
    private JsonSerializer jsonSerializer;

    @InjectMocks
    private KrakenJsonOrderBookObjectConverter converter;

    @Test
    public void testSubscriptionResponse() {
        // setup
        String json = "{\"method\":\"subscribe\",\"req_id\":1234567890,\"result\":{\"channel\":\"book\",\"depth\":10,\"snapshot\":true,\"symbol\":\"BTC/USD\"},\"success\":true,\"time_in\":\"2023-05-27T21:03:08.413713Z\",\"time_out\":\"2023-05-27T21:03:08.413753Z\"}";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNotNull(converter.getSubscriptionResponse());
        assertNull(converter.getOrderBookInfo());

        assertEquals(KrakenSubscriptionResponseOrderBook.class, converter.objectTypeParsed());

        KrakenSubscriptionResponseOrderBook subscriptionResponse = converter.getSubscriptionResponse();

        assertEquals("subscribe", subscriptionResponse.getMethod());
        assertEquals(1234567890L, subscriptionResponse.getReq_id().longValue());
        assertTrue(subscriptionResponse.isSuccess());
        assertEquals(Instant.parse("2023-05-27T21:03:08.413713Z"), subscriptionResponse.getTime_in());
        assertEquals(Instant.parse("2023-05-27T21:03:08.413753Z"), subscriptionResponse.getTime_out());

        KrakenSubscriptionResponseOrderBookResult result = subscriptionResponse.getResult();
        assertEquals("book", result.getChannel());
        assertEquals(10, result.getDepth());
        assertTrue(result.isSnapshot());
        assertEquals("BTC/USD", result.getSymbol());
    }

    @Test
    public void testOrderBookSnapshot() {
        // setup
        String json = "{\"channel\":\"book\",\"type\":\"snapshot\",\"data\":[{\"symbol\":\"BTC/USD\",\"bids\":[{\"price\":26807.9,\"qty\":2.41988861},{\"price\":26807.3,\"qty\":0.00105605},{\"price\":26807.2,\"qty\":0.04659203},{\"price\":26807.0,\"qty\":2.89373301},{\"price\":26806.9,\"qty\":2.61104515},{\"price\":26806.8,\"qty\":2.79779464},{\"price\":26805.7,\"qty\":0.07159344},{\"price\":26805.2,\"qty\":0.04680000},{\"price\":26804.9,\"qty\":0.08951676},{\"price\":26804.8,\"qty\":1.41941983}],\"asks\":[{\"price\":26808.0,\"qty\":0.25325256},{\"price\":26812.3,\"qty\":0.01523107},{\"price\":26815.4,\"qty\":0.07959194},{\"price\":26815.5,\"qty\":0.55956160},{\"price\":26815.9,\"qty\":2.79685245},{\"price\":26816.0,\"qty\":0.93585796},{\"price\":26816.3,\"qty\":0.55952612},{\"price\":26817.4,\"qty\":0.65063008},{\"price\":26819.5,\"qty\":0.15000010},{\"price\":26819.6,\"qty\":0.04680000}],\"checksum\":4171994782}]}";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNull(converter.getSubscriptionResponse());
        assertNotNull(converter.getOrderBookInfo());

        assertEquals(KrakenOrderBookInfo.class, converter.objectTypeParsed());

        KrakenOrderBookInfo orderBookInfo = converter.getOrderBookInfo();

        assertEquals("book", orderBookInfo.getChannel());
        assertEquals("snapshot", orderBookInfo.getType());
        assertTrue(orderBookInfo.isSnapshot());
        assertEquals(1, orderBookInfo.getData().size());

        KrakenOrderBook2Data data = orderBookInfo.getData().get(0);

        assertEquals("BTC/USD", data.getSymbol());
        assertEquals(4171994782L, data.getChecksum());
        assertNull(data.getTimestamp());

        List<KrakenOrderBookLevel> bids = data.getBids();
        assertEquals(10, bids.size());
        assertLevelEquals(bids.get(0), 26807.9, 2.41988861);
        assertLevelEquals(bids.get(1), 26807.3, 0.00105605);
        assertLevelEquals(bids.get(2), 26807.2, 0.04659203);
        assertLevelEquals(bids.get(3), 26807.0, 2.89373301);
        assertLevelEquals(bids.get(4), 26806.9, 2.61104515);
        assertLevelEquals(bids.get(5), 26806.8, 2.79779464);
        assertLevelEquals(bids.get(6), 26805.7, 0.07159344);
        assertLevelEquals(bids.get(7), 26805.2, 0.04680000);
        assertLevelEquals(bids.get(8), 26804.9, 0.08951676);
        assertLevelEquals(bids.get(9), 26804.8, 1.41941983);

        List<KrakenOrderBookLevel> asks = data.getAsks();
        assertEquals(10, asks.size());
        assertLevelEquals(asks.get(0), 26808.0, 0.25325256);
        assertLevelEquals(asks.get(1), 26812.3, 0.01523107);
        assertLevelEquals(asks.get(2), 26815.4, 0.07959194);
        assertLevelEquals(asks.get(3), 26815.5, 0.55956160);
        assertLevelEquals(asks.get(4), 26815.9, 2.79685245);
        assertLevelEquals(asks.get(5), 26816.0, 0.93585796);
        assertLevelEquals(asks.get(6), 26816.3, 0.55952612);
        assertLevelEquals(asks.get(7), 26817.4, 0.65063008);
        assertLevelEquals(asks.get(8), 26819.5, 0.15000010);
        assertLevelEquals(asks.get(9), 26819.6, 0.04680000);
    }

    @Test
    public void testOrderBookUpdateBids() {
        // setup
        String json = "{\"channel\":\"book\",\"type\":\"update\",\"data\":[{\"symbol\":\"BTC/USD\",\"bids\":[{\"price\":27013.4,\"qty\":2.77639575}],\"asks\":[],\"checksum\":306164425,\"timestamp\":\"2023-05-28T00:42:36.690934Z\"}]}";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNull(converter.getSubscriptionResponse());
        assertNotNull(converter.getOrderBookInfo());

        assertEquals(KrakenOrderBookInfo.class, converter.objectTypeParsed());

        KrakenOrderBookInfo orderBookInfo = converter.getOrderBookInfo();

        assertEquals("book", orderBookInfo.getChannel());
        assertEquals("update", orderBookInfo.getType());
        assertFalse(orderBookInfo.isSnapshot());

        assertEquals(1, orderBookInfo.getData().size());
        KrakenOrderBook2Data data = orderBookInfo.getData().get(0);
        assertEquals("BTC/USD", data.getSymbol());
        assertEquals(306164425L, data.getChecksum());
        assertEquals(Instant.parse("2023-05-28T00:42:36.690934Z"), data.getTimestamp());

        assertEquals(1, data.getBids().size());
        KrakenOrderBookLevel bid = data.getBids().get(0);
        assertLevelEquals(bid, 27013.4, 2.77639575);
        assertTrue(data.getAsks().isEmpty());
    }

    @Test
    public void testOrderBookUpdateAsks() {
        // setup
        String json = "{\"channel\":\"book\",\"type\":\"update\",\"data\":[{\"symbol\":\"BTC/USD\",\"bids\":[],\"asks\":[{\"price\":27036.2,\"qty\":0.74450000}],\"checksum\":484646001,\"timestamp\":\"2023-05-28T00:42:36.707767Z\"}]}";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNull(converter.getSubscriptionResponse());
        assertNotNull(converter.getOrderBookInfo());

        assertEquals(KrakenOrderBookInfo.class, converter.objectTypeParsed());

        KrakenOrderBookInfo orderBookInfo = converter.getOrderBookInfo();

        assertEquals("book", orderBookInfo.getChannel());
        assertEquals("update", orderBookInfo.getType());
        assertFalse(orderBookInfo.isSnapshot());

        assertEquals(1, orderBookInfo.getData().size());
        KrakenOrderBook2Data data = orderBookInfo.getData().get(0);
        assertEquals("BTC/USD", data.getSymbol());
        assertEquals(484646001L, data.getChecksum());
        assertEquals(Instant.parse("2023-05-28T00:42:36.707767Z"), data.getTimestamp());

        assertTrue(data.getBids().isEmpty());
        assertEquals(1, data.getAsks().size());
        KrakenOrderBookLevel ask = data.getAsks().get(0);
        assertLevelEquals(ask, 27036.2, 0.74450000);
    }

    private void assertLevelEquals(KrakenOrderBookLevel level, double expectedPrice, double expectedQuantity) {
        assertEquals(expectedPrice, level.getPrice(), DOUBLE_COMPARE_DELTA);
        assertEquals(expectedQuantity, level.getQty(), DOUBLE_COMPARE_DELTA);
    }

}
