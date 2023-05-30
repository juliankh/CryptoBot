package com.cb.ws.kraken;

import com.cb.model.CbOrderBook;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ChecksumCalculatorTest {

    @InjectMocks
    private ChecksumCalculator checksumCalculator;

    @Test
    public void checksum_String() {
        assertEquals(187053740, checksumCalculator.checksum("3501001000000350200200000035030030000003504004000000350500500000035060060000003507007000000350800800000035090090000003510001000000034100010000000340900900000034080080000003407007000000340600600000034050050000003404004000000340300300000034020020000003401001000000"));
        assertEquals(366254901, checksumCalculator.checksum("347264250000003472772100000034727850000003473163459000034731771035573473192880253634733072004074347362319882093473801430000034739024041000347176130000003471418572098347139380000003471262880800347118860000003471021679785643471007199064434709326085746347079160371663470785682871"));
    }

    @Test
    public void topNEntries() {
        TreeMap<Integer, Integer> map = new TreeMap<>(){{
            put(1, 11);
            put(2, 22);
            put(3, 33);
            put(4, 44);
            put(5, 55);
        }};
        assertEquals(Collections.emptyList(), checksumCalculator.topNEntries(map, 0));
        assertEquals(Lists.newArrayList(Pair.of(1, 11)), checksumCalculator.topNEntries(map, 1));
        assertEquals(Lists.newArrayList(Pair.of(5, 55)), checksumCalculator.topNEntries(map.descendingMap(), 1));
        assertEquals(Lists.newArrayList(Pair.of(1, 11), Pair.of(2, 22), Pair.of(3, 33)), checksumCalculator.topNEntries(map, 3));
        assertEquals(Lists.newArrayList(Pair.of(5, 55), Pair.of(4, 44), Pair.of(3, 33)), checksumCalculator.topNEntries(map.descendingMap(), 3));
        assertEquals(Lists.newArrayList(Pair.of(1, 11), Pair.of(2, 22), Pair.of(3, 33), Pair.of(4, 44), Pair.of(5, 55)), checksumCalculator.topNEntries(map, 15));
    }

    @Test
    public void digestForNumber() {
        assertEquals("350100", checksumCalculator.digestForNumber(0.3501, 6));
        assertEquals("341000", checksumCalculator.digestForNumber(0.3410, 6));
        assertEquals("1000000", checksumCalculator.digestForNumber(0.01, 8));
        assertEquals("10000000", checksumCalculator.digestForNumber(0.10, 8));
    }

    @Test
    public void digestForLevel() {
        assertEquals("3501001000000", checksumCalculator.digestForLevel(0.3501, 0.01, 6, 8));
        assertEquals("34100010000000", checksumCalculator.digestForLevel(0.3410, 0.10, 6, 8));
    }

    @Test
    public void checksum_OrderBook1() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            put(0.3410, 0.10);
            put(0.3409, 0.09);
            put(0.3408, 0.08);
            put(0.3407, 0.07);
            put(0.3406, 0.06);
            put(0.3405, 0.05);
            put(0.3404, 0.04);
            put(0.3403, 0.03);
            put(0.3402, 0.02);
            put(0.3401, 0.01);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            put(0.3501, 0.01);
            put(0.3502, 0.02);
            put(0.3503, 0.03);
            put(0.3504, 0.04);
            put(0.3505, 0.05);
            put(0.3506, 0.06);
            put(0.3507, 0.07);
            put(0.3508, 0.08);
            put(0.3509, 0.09);
            put(0.3510, 0.10);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks);
        assertEquals(187053740L, checksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook2() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            put(26804.8, 1.41941983);
            put(26804.9, 0.08951676);
            put(26805.2, 0.04680000);
            put(26805.7, 0.07159344);
            put(26806.8, 2.79779464);
            put(26806.9, 2.61104515);
            put(26807.0, 2.89373301);
            put(26807.2, 0.04659203);
            put(26807.3, 0.00105605);
            put(26807.9, 2.41988861);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            put(26808.0, 0.25325256);
            put(26812.3, 0.01523107);
            put(26815.4, 0.07959194);
            put(26815.5, 0.55956160);
            put(26815.9, 2.79685245);
            put(26816.0, 0.93585796);
            put(26816.3, 0.55952612);
            put(26817.4, 0.65063008);
            put(26819.5, 0.15000010);
            put(26819.6, 0.04680000);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks);
        assertEquals(4171994782L, checksumCalculator.checksum(orderBook));
    }

    // from email with Kraken support
    @Test
    public void checksum_OrderBook3() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            put(27832.0, 28.92705116);
            put(27830.8, 0.04794682);
            put(27828.0, 0.00900000);
            put(27827.7, 0.03700000);
            put(27824.0, 0.00688991);
            put(27822.0, 0.10773139);
            put(27820.8, 0.40000000);
            put(27818.1, 2.65932807);
            put(27818.0, 0.40000000);
            put(27817.9, 2.69609757);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            put(27832.1, 9.33036477);
            put(27832.2, 1.42218843);
            put(27832.3, 7.18592092);
            put(27832.5, 0.08954298);
            put(27832.6, 3.64258945);
            put(27833.3, 0.07162749);
            put(27833.6, 0.53901382);
            put(27833.7, 0.02430000);
            put(27833.8, 0.58378660);
            put(27834.0, 0.04811906);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks);
        assertEquals(213973217L, checksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook4() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            put(34717.6, 0.13);
            put(34714.1, 0.08572098);
            put(34713.9, 0.38);
            put(34712.6, 0.028808);
            put(34711.8, 0.86);
            put(34710.2, 1.67978564);
            put(34710.0, 0.71990644);
            put(34709.3, 0.26085746);
            put(34707.9, 0.16037166);
            put(34707.8, 0.05682871);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            put(34726.4, 0.25);
            put(34727.7, 0.21);
            put(34727.8, 0.05);
            put(34731.6, 0.3459);
            put(34731.7, 0.07103557);
            put(34731.9, 0.28802536);
            put(34733.0, 0.72004074);
            put(34736.2, 0.31988209);
            put(34738.0, 0.143);
            put(34739.0, 0.24041);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks);
        assertEquals(366254901L, checksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook5() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            put(27752.3, 11.82377683);
            put(27751.1, 0.03579528);
            put(27750.9, 0.01189931);
            put(27750.6, 0.10810000);
            put(27749.6, 0.18183279);
            put(27749.5, 0.37500000);
            put(27748.5, 0.08951676);
            put(27748.2, 0.01000000);
            put(27748.1, 0.37500000);
            put(27746.7, 0.37500000);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            put(27752.4, 0.00535379);
            put(27757.3, 0.15752500);
            put(27757.5, 2.70197658);
            put(27760.7, 0.01000000);
            put(27763.6, 0.37500000);
            put(27763.8, 0.00571814);
            put(27764.3, 0.01600000);
            put(27764.9, 0.33068522);
            put(27765.0, 2.80894977);
            put(27766.3, 2.59365894);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks);
        assertEquals(1161112910L, checksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook6() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            put(0.49152, 1000.00000000);
            put(0.49137, 506.82172038);
            put(0.49136, 9742.07099503);
            put(0.49135, 5050.00000000);
            put(0.49132, 7000.00000000);
            put(0.49131, 20.32118214);
            put(0.49130, 15926.77318728);
            put(0.49126, 4174.85253974);
            put(0.49125, 10130.00000000);
            put(0.49124, 10178.24868188);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            put(0.49157, 20100.56163031);
            put(0.49165, 10169.87763603);
            put(0.49170, 1235.00000000);
            put(0.49171, 2000.00000000);
            put(0.49172, 2000.00000000);
            put(0.49173, 10168.34715550);
            put(0.49175, 5050.00000000);
            put(0.49176, 3142.34838169);
            put(0.49177, 328.23127806);
            put(0.49184, 1832.10121544);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks);
        assertEquals(780937287L, checksumCalculator.checksum(orderBook));
    }

}
