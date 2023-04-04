package com.cb.db;

import com.cb.model.orderbook.DbKrakenOrderbook;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DbProviderTest {

    private static final DbProvider DB_PROVIDER = new DbProvider();

    @Test
    public void numDupes() {
        assertEquals(3, DB_PROVIDER.numDupes(Lists.newArrayList(
                    Lists.newArrayList(2, 2),     // 1 dupe
                    Lists.newArrayList(1, 1, 1),  // 2 dupes
                    Lists.newArrayList(3)         // 0 dupes
        )));
    }

    @Test
    public void dupeDescription() {
        assertEquals("# dupe ItemType1 received from upstream [0]; # of ItemType1 rows skipped insertion [0] [NONE]", DB_PROVIDER.dupeDescription("ItemType1", 0, 0));
        assertEquals("# dupe ItemType1 received from upstream [3]; # of ItemType1 rows skipped insertion [3]", DB_PROVIDER.dupeDescription("ItemType1", 3, 3));
        assertEquals("# dupe ItemType1 received from upstream [3]; # of ItemType1 rows skipped insertion [2] [DIFFERENT] (probably due to multiple persisters running concurrently)", DB_PROVIDER.dupeDescription("ItemType1", 3, 2));
    }

    @Test
    public void dupeOrderBooks_Dupes() {
        DbKrakenOrderbook orderbook1 = new DbKrakenOrderbook();orderbook1.setBids_hash(111);orderbook1.setAsks_hash(1111);orderbook1.setId(123L);
        DbKrakenOrderbook orderbook2 = new DbKrakenOrderbook();orderbook2.setBids_hash(222);orderbook2.setAsks_hash(2222);orderbook2.setId(124L);
        DbKrakenOrderbook orderbook3 = new DbKrakenOrderbook();orderbook3.setBids_hash(222);orderbook3.setAsks_hash(2222);orderbook3.setId(125L);
        DbKrakenOrderbook orderbook4 = new DbKrakenOrderbook();orderbook4.setBids_hash(111);orderbook4.setAsks_hash(1111);orderbook4.setId(126L);
        DbKrakenOrderbook orderbook5 = new DbKrakenOrderbook();orderbook5.setBids_hash(333);orderbook5.setAsks_hash(3333);orderbook5.setId(127L);
        List<DbKrakenOrderbook> convertedBatch = Lists.newArrayList(orderbook1, orderbook2, orderbook3, orderbook4, orderbook5);

        // engage test
        Map<Pair<Integer, Integer>, List<DbKrakenOrderbook>> result = DB_PROVIDER.dupeOrderBooks(convertedBatch);

        assertEquals(2, result.size());
        assertEquals(Sets.newHashSet(Pair.of(111, 1111), Pair.of(222, 2222)), result.keySet());
        assertEquals(Lists.newArrayList(orderbook1, orderbook4), result.get(Pair.of(111, 1111)));
        assertEquals(Lists.newArrayList(orderbook2, orderbook3), result.get(Pair.of(222, 2222)));
    }

    @Test
    public void dupeOrderBooks_NoDupes() {
        DbKrakenOrderbook orderbook1 = new DbKrakenOrderbook();orderbook1.setBids_hash(111);orderbook1.setAsks_hash(1111);orderbook1.setId(123L);
        DbKrakenOrderbook orderbook2 = new DbKrakenOrderbook();orderbook2.setBids_hash(222);orderbook2.setAsks_hash(2222);orderbook2.setId(124L);
        DbKrakenOrderbook orderbook3 = new DbKrakenOrderbook();orderbook3.setBids_hash(333);orderbook3.setAsks_hash(3333);orderbook3.setId(125L);
        DbKrakenOrderbook orderbook4 = new DbKrakenOrderbook();orderbook4.setBids_hash(444);orderbook4.setAsks_hash(4444);orderbook4.setId(126L);
        DbKrakenOrderbook orderbook5 = new DbKrakenOrderbook();orderbook5.setBids_hash(555);orderbook5.setAsks_hash(5555);orderbook5.setId(127L);
        List<DbKrakenOrderbook> convertedBatch = Lists.newArrayList(orderbook1, orderbook2, orderbook3, orderbook4, orderbook5);

        // engage test
        Map<Pair<Integer, Integer>, List<DbKrakenOrderbook>> result = DB_PROVIDER.dupeOrderBooks(convertedBatch);

        assertEquals(0, result.size());
    }

    @Test
    public void checkUpsertRowCounts_NoException() {
        assertEquals(0, DB_PROVIDER.checkUpsertRowCounts(new int[]{}));
        assertEquals(0, DB_PROVIDER.checkUpsertRowCounts(new int[]{1}));
        assertEquals(1, DB_PROVIDER.checkUpsertRowCounts(new int[]{0}));
        assertEquals(2, DB_PROVIDER.checkUpsertRowCounts(new int[]{1, 0, 0}));
    }

    @Test(expected = RuntimeException.class)
    public void checkUpsertRowCounts_Exception() {
        assertEquals(1, DB_PROVIDER.checkUpsertRowCounts(new int[]{1, 0, 2}));
    }

}
