package com.cb.db;

import com.cb.common.util.TimeUtils;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;



@ExtendWith(MockitoExtension.class)
public class DbWriteProviderTest {

    @InjectMocks
    private DbWriteProvider dbWriteProvider;

    @Test
    public void numDupes() {
        assertEquals(3, dbWriteProvider.numDupes(Lists.newArrayList(
                    Lists.newArrayList(2, 2),     // 1 dupe
                    Lists.newArrayList(1, 1, 1),  // 2 dupes
                    Lists.newArrayList(3)         // 0 dupes
        )));
    }

    @Test
    public void dupeDescription() {
        assertEquals("# dupe ItemType1 received from upstream [0]; # of ItemType1 rows skipped insertion [0] [NONE]", dbWriteProvider.dupeDescription("ItemType1", 0, 0));
        assertEquals("# dupe ItemType1 received from upstream [3]; # of ItemType1 rows skipped insertion [3]", dbWriteProvider.dupeDescription("ItemType1", 3, 3));
        assertEquals("# dupe ItemType1 received from upstream [3]; # of ItemType1 rows skipped insertion [2] [DIFFERENT by (-1)] (probably due to multiple persisters running concurrently)", dbWriteProvider.dupeDescription("ItemType1", 3, 2));
        assertEquals("# dupe ItemType1 received from upstream [3]; # of ItemType1 rows skipped insertion [5] [DIFFERENT by (2)] (probably due to multiple persisters running concurrently)", dbWriteProvider.dupeDescription("ItemType1", 3, 5));
    }

    @Test
    public void dupeOrderBooks_Dupes() {
        long receivedMicros = TimeUtils.currentMicros();
        DbKrakenOrderBook orderbook1 = new DbKrakenOrderBook();orderbook1.setReceived_micros(receivedMicros);orderbook1.setBids_hash(111);orderbook1.setAsks_hash(1111);orderbook1.setId(123L);
        DbKrakenOrderBook orderbook2 = new DbKrakenOrderBook();orderbook2.setReceived_micros(receivedMicros);orderbook2.setBids_hash(222);orderbook2.setAsks_hash(2222);orderbook2.setId(124L);
        DbKrakenOrderBook orderbook3 = new DbKrakenOrderBook();orderbook3.setReceived_micros(receivedMicros);orderbook3.setBids_hash(222);orderbook3.setAsks_hash(2222);orderbook3.setId(125L);
        DbKrakenOrderBook orderbook4 = new DbKrakenOrderBook();orderbook4.setReceived_micros(receivedMicros);orderbook4.setBids_hash(111);orderbook4.setAsks_hash(1111);orderbook4.setId(126L);
        DbKrakenOrderBook orderbook5 = new DbKrakenOrderBook();orderbook5.setReceived_micros(receivedMicros);orderbook5.setBids_hash(333);orderbook5.setAsks_hash(3333);orderbook5.setId(127L);
        List<DbKrakenOrderBook> convertedBatch = Lists.newArrayList(orderbook1, orderbook2, orderbook3, orderbook4, orderbook5);

        // engage test
        Map<Triple<Long, Integer, Integer>, List<DbKrakenOrderBook>> result = dbWriteProvider.dupeOrderBooks(convertedBatch);

        assertEquals(2, result.size());
        assertEquals(Sets.newHashSet(Triple.of(receivedMicros, 111, 1111), Triple.of(receivedMicros, 222, 2222)), result.keySet());
        assertEquals(Lists.newArrayList(orderbook1, orderbook4), result.get(Triple.of(receivedMicros, 111, 1111)));
        assertEquals(Lists.newArrayList(orderbook2, orderbook3), result.get(Triple.of(receivedMicros, 222, 2222)));
    }

    @Test
    public void dupeOrderBooks_NoDupes() {
        long receivedNanos = TimeUtils.currentMicros();
        DbKrakenOrderBook orderbook1 = new DbKrakenOrderBook();orderbook1.setReceived_micros(receivedNanos);orderbook1.setBids_hash(111);orderbook1.setAsks_hash(1111);orderbook1.setId(123L);
        DbKrakenOrderBook orderbook2 = new DbKrakenOrderBook();orderbook2.setReceived_micros(receivedNanos);orderbook2.setBids_hash(222);orderbook2.setAsks_hash(2222);orderbook2.setId(124L);
        DbKrakenOrderBook orderbook3 = new DbKrakenOrderBook();orderbook3.setReceived_micros(receivedNanos);orderbook3.setBids_hash(333);orderbook3.setAsks_hash(3333);orderbook3.setId(125L);
        DbKrakenOrderBook orderbook4 = new DbKrakenOrderBook();orderbook4.setReceived_micros(receivedNanos);orderbook4.setBids_hash(444);orderbook4.setAsks_hash(4444);orderbook4.setId(126L);
        DbKrakenOrderBook orderbook5 = new DbKrakenOrderBook();orderbook5.setReceived_micros(receivedNanos);orderbook5.setBids_hash(555);orderbook5.setAsks_hash(5555);orderbook5.setId(127L);
        List<DbKrakenOrderBook> orderbooks = Lists.newArrayList(orderbook1, orderbook2, orderbook3, orderbook4, orderbook5);

        // engage test
        Map<Triple<Long, Integer, Integer>, List<DbKrakenOrderBook>> result = dbWriteProvider.dupeOrderBooks(orderbooks);

        assertEquals(0, result.size());
    }

    @Test
    public void checkUpsertRowCounts_NoException() {
        assertEquals(0, dbWriteProvider.checkUpsertRowCounts(new int[]{}));
        assertEquals(0, dbWriteProvider.checkUpsertRowCounts(new int[]{1}));
        assertEquals(1, dbWriteProvider.checkUpsertRowCounts(new int[]{0}));
        assertEquals(2, dbWriteProvider.checkUpsertRowCounts(new int[]{1, 0, 0}));
    }

    @Test
    public void checkUpsertRowCounts_Exception() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dbWriteProvider.checkUpsertRowCounts(new int[]{1, 0, 2}));
        assertEquals("1 RowCounts have value different from the expected [0 or 1] after insertion into db: 2", exception.getMessage());
    }

    @Test
    public void questionMarks() {
        assertEquals("?", dbWriteProvider.questionMarks(1));
        assertEquals("?,?", dbWriteProvider.questionMarks(2));
        assertEquals("?,?,?,?", dbWriteProvider.questionMarks(4));
    }

}
