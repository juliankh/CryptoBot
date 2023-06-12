package com.cb.ws.kraken;

import com.cb.common.JsonSerializer;
import com.cb.model.kraken.ws.response.KrakenError;
import com.cb.model.kraken.ws.response.KrakenHeartbeat;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdate;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class KrakenJsonAbstractObjectConverterTest {

    @Spy
    private JsonSerializer jsonSerializer;

    @InjectMocks
    private KrakenJsonOrderBookObjectConverter converter; // using KrakenOrderBookJsonObjectConverter instead of KrakenAbstractJsonObjectConverter because can't instantiate an abstract class

    @Test
    public void testError() {
        // setup
        String json = "{\"error\":\"Subscription depth not supported\",\"method\":\"subscribe\",\"req_id\":1234567890,\"success\":false,\"time_in\":\"2023-05-28T01:03:37.445243Z\",\"time_out\":\"2023-05-28T01:03:37.445279Z\"}";

        // engage test
        converter.parse(json);

        // verify
        assertNotNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNull(converter.getSubscriptionResponse());
        assertNull(converter.getOrderBookInfo());

        assertEquals(KrakenError.class, converter.objectTypeParsed());

        KrakenError error = converter.getError();

        assertEquals("Subscription depth not supported", error.getError());
        assertEquals("subscribe", error.getMethod());
        assertEquals(1234567890L, error.getReq_id().longValue());
        assertFalse(error.isSuccess());
        assertEquals(Instant.parse("2023-05-28T01:03:37.445243Z"), error.getTime_in());
        assertEquals(Instant.parse("2023-05-28T01:03:37.445279Z"), error.getTime_out());
    }

    @Test
    public void testStatusUpdate() {
        // setup
        String json = "{\"channel\":\"status\",\"data\":[{\"api_version\":\"v2\",\"connection_id\":10878232658551185331,\"system\":\"online\",\"version\":\"2.0.0\"}],\"type\":\"update\"}";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNotNull(converter.getStatusUpdate());
        assertNull(converter.getHeartbeat());
        assertNull(converter.getSubscriptionResponse());
        assertNull(converter.getOrderBookInfo());

        assertEquals(KrakenStatusUpdate.class, converter.objectTypeParsed());

        KrakenStatusUpdate statusUpdate = converter.getStatusUpdate();

        assertEquals("status", statusUpdate.getChannel());
        assertEquals("update", statusUpdate.getType());
        assertEquals(1, statusUpdate.getData().size());

        KrakenStatusUpdateData updateData = statusUpdate.getData().get(0);
        assertEquals("v2", updateData.getApi_version());
        assertEquals(new BigInteger("10878232658551185331"), updateData.getConnection_id());
        assertEquals("online", updateData.getSystem());
        assertEquals("2.0.0", updateData.getVersion());
    }

    @Test
    public void testHeartbeat() {
        // setup
        String json = "{\"channel\":\"heartbeat\"}";

        // engage test
        converter.parse(json);

        // verify
        assertNull(converter.getError());
        assertNull(converter.getStatusUpdate());
        assertNotNull(converter.getHeartbeat());
        assertNull(converter.getSubscriptionResponse());
        assertNull(converter.getOrderBookInfo());

        assertEquals(KrakenHeartbeat.class, converter.objectTypeParsed());

        KrakenHeartbeat heartbeat = converter.getHeartbeat();

        assertEquals("heartbeat", heartbeat.getChannel());
    }

    @Test
    public void testEmptyJson() {
        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> converter.parse(null));
        assertEquals("JSON is empty: [null]", exception1.getMessage());

        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> converter.parse(""));
        assertEquals("JSON is empty: []", exception2.getMessage());
    }

    @Test
    public void testUnparsableJson() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> converter.parse("unparsable json hey unparsable json hey unparsable json hey unparsable json hey unparsable json hey unparsable json hey "));
        assertEquals("Don't know how to parse this json: <unparsable json hey unparsable json hey unparsable json hey unparsable json hey unparsable json hey ...>", exception.getMessage());
    }

}
