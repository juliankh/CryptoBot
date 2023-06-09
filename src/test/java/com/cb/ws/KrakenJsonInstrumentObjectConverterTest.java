package com.cb.ws;

import com.cb.common.JsonSerializer;
import com.cb.model.kraken.ws.response.instrument.KrakenAsset;
import com.cb.model.kraken.ws.response.instrument.KrakenAssetPair;
import com.cb.model.kraken.ws.response.instrument.KrakenInstrumentData;
import com.cb.model.kraken.ws.response.instrument.KrakenInstrumentInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseInstrument;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseInstrumentResult;
import com.cb.ws.kraken.json_converter.KrakenJsonInstrumentObjectConverter;
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
public class KrakenJsonInstrumentObjectConverterTest {

    @Spy
    private JsonSerializer jsonSerializer;

    @InjectMocks
    private KrakenJsonInstrumentObjectConverter converter;
    
    @Test
    public void testSubscriptionResponse() {
        // setup
        String json = """
                {
                   "method":"subscribe",
                   "req_id":134679,
                   "result":{
                      "channel":"instrument",
                      "snapshot":true
                   },
                   "success":true,
                   "time_in":"2023-06-07T20:34:50.913987Z",
                   "time_out":"2023-06-07T20:34:50.914044Z"
                }""";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNotNull(converter.getSubscriptionResponse());
        assertNull(converter.getInstrumentInfo());

        assertEquals(KrakenSubscriptionResponseInstrument.class, converter.objectTypeParsed());

        KrakenSubscriptionResponseInstrument subscriptionResponse = converter.getSubscriptionResponse();

        assertEquals("subscribe", subscriptionResponse.getMethod());
        assertEquals(134679L, subscriptionResponse.getReq_id().longValue());
        assertTrue(subscriptionResponse.isSuccess());
        assertEquals(Instant.parse("2023-06-07T20:34:50.913987Z"), subscriptionResponse.getTime_in());
        assertEquals(Instant.parse("2023-06-07T20:34:50.914044Z"), subscriptionResponse.getTime_out());

        KrakenSubscriptionResponseInstrumentResult result = subscriptionResponse.getResult();
        assertEquals("instrument", result.getChannel());
        assertTrue(result.isSnapshot());
    }

    @Test
    public void testInstrumentSnapshot() {
        // setup
        String json = """
                {
                   "channel":"instrument",
                   "type":"snapshot",
                   "data":{
                      "assets":[
                         {
                            "id":"USD",
                            "status":"enabled",
                            "precision":4,
                            "precision_display":2,
                            "borrowable":true,
                            "collateral_value":1.00,
                            "margin_rate":0.015000
                         },
                         {
                            "id":"EUR",
                            "status":"another_status",
                            "precision":4,
                            "precision_display":3,
                            "borrowable":false,
                            "collateral_value":0.90,
                            "margin_rate":0.025000
                         }
                      ],
                      "pairs":[
                         {
                            "symbol":"EUR/USD",
                            "base":"EUR",
                            "quote":"USD",
                            "status":"online",
                            "qty_precision":8,
                            "qty_increment":0.00000001,
                            "price_precision":5,
                            "cost_precision":5,
                            "marginable":false,
                            "has_index":false,
                            "cost_min":0.50,
                            "tick_size":0.00001,
                            "price_increment":0.00001,
                            "qty_min":0.50000000
                         },
                         {
                            "symbol":"GBP/USD",
                            "base":"GBP",
                            "quote":"USD",
                            "status":"different_status",
                            "qty_precision":9,
                            "qty_increment":0.00000002,
                            "price_precision":6,
                            "cost_precision":7,
                            "marginable":true,
                            "has_index":true,
                            "cost_min":0.54,
                            "tick_size":0.00003,
                            "price_increment":0.00004,
                            "qty_min":0.75000000
                         }
                      ]
                   }
                }""";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNull(converter.getSubscriptionResponse());
        assertNotNull(converter.getInstrumentInfo());

        assertEquals(KrakenInstrumentInfo.class, converter.objectTypeParsed());

        KrakenInstrumentInfo instrumentInfo = converter.getInstrumentInfo();

        assertEquals("instrument", instrumentInfo.getChannel());
        assertEquals("snapshot", instrumentInfo.getType());
        assertTrue(instrumentInfo.isSnapshot());

        KrakenInstrumentData data = instrumentInfo.getData();
        List<KrakenAsset> assets = data.getAssets();
        List<KrakenAssetPair> pairs = data.getPairs();

        assertEquals(2, assets.size());
        assertEquals(2, pairs.size());

        KrakenAsset asset1 = assets.get(0);
        KrakenAsset asset2 = assets.get(1);

        assertEquals("USD", asset1.getId());
        assertEquals("enabled", asset1.getStatus());
        assertEquals(4, asset1.getPrecision());
        assertEquals(2, asset1.getPrecision_display());
        assertTrue(asset1.isBorrowable());
        assertEquals(1.0, asset1.getCollateral_value(), DOUBLE_COMPARE_DELTA);
        assertEquals(0.015, asset1.getMargin_rate(), DOUBLE_COMPARE_DELTA);

        assertEquals("EUR", asset2.getId());
        assertEquals("another_status", asset2.getStatus());
        assertEquals(4, asset2.getPrecision());
        assertEquals(3, asset2.getPrecision_display());
        assertFalse(asset2.isBorrowable());
        assertEquals(0.9, asset2.getCollateral_value(), DOUBLE_COMPARE_DELTA);
        assertEquals(0.025, asset2.getMargin_rate(), DOUBLE_COMPARE_DELTA);

        KrakenAssetPair pair1 = pairs.get(0);
        KrakenAssetPair pair2 = pairs.get(1);

        assertEquals("EUR/USD", pair1.getSymbol());
        assertEquals("EUR", pair1.getBase());
        assertEquals("USD", pair1.getQuote());
        assertEquals("online", pair1.getStatus());
        assertEquals(8, pair1.getQty_precision());
        assertEquals(0.00000001, pair1.getQty_increment(), DOUBLE_COMPARE_DELTA);
        assertEquals(5, pair1.getPrice_precision());
        assertEquals(5, pair1.getCost_precision());
        assertFalse(pair1.isMarginable());
        assertFalse(pair1.isHas_index());
        assertEquals(0.5, pair1.getCost_min(), DOUBLE_COMPARE_DELTA);
        assertEquals(0.00001, pair1.getPrice_increment(), DOUBLE_COMPARE_DELTA);
        assertEquals(0.5, pair1.getQty_min(), DOUBLE_COMPARE_DELTA);

        assertEquals("GBP/USD", pair2.getSymbol());
        assertEquals("GBP", pair2.getBase());
        assertEquals("USD", pair2.getQuote());
        assertEquals("different_status", pair2.getStatus());
        assertEquals(9, pair2.getQty_precision());
        assertEquals(0.00000002, pair2.getQty_increment(), DOUBLE_COMPARE_DELTA);
        assertEquals(6, pair2.getPrice_precision());
        assertEquals(7, pair2.getCost_precision());
        assertTrue(pair2.isMarginable());
        assertTrue(pair2.isHas_index());
        assertEquals(0.54, pair2.getCost_min(), DOUBLE_COMPARE_DELTA);
        assertEquals(0.00004, pair2.getPrice_increment(), DOUBLE_COMPARE_DELTA);
        assertEquals(0.75, pair2.getQty_min(), DOUBLE_COMPARE_DELTA);
    }

}
