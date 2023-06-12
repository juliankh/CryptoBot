package com.cb.processor.checksum;

import com.cb.model.CbOrderBook;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class KrakenChecksumCalculatorTest {

    private static final Map<CurrencyPair, Pair<Integer, Integer>> PRECISION_MAP = new HashMap<>() {{
        put(CurrencyPair.ADA_USD, Pair.of(6, 8));
        put(CurrencyPair.BTC_USDT, Pair.of(1, 8));
        put(CurrencyPair.XRP_USD, Pair.of(5, 8));
        put(new CurrencyPair(Currency.BTT, Currency.USD), Pair.of(8, 5));
        put(CurrencyPair.BTC_JPY, Pair.of(0, 8));
    }};

    @InjectMocks
    private KrakenChecksumCalculator krakenChecksumCalculator;

    @BeforeEach
    public void beforeEachTest() {
        krakenChecksumCalculator.initialize(PRECISION_MAP);
    }

    @Test
    public void checksum_String() {
        assertEquals(187053740, krakenChecksumCalculator.checksum("3501001000000350200200000035030030000003504004000000350500500000035060060000003507007000000350800800000035090090000003510001000000034100010000000340900900000034080080000003407007000000340600600000034050050000003404004000000340300300000034020020000003401001000000"));
        assertEquals(366254901, krakenChecksumCalculator.checksum("347264250000003472772100000034727850000003473163459000034731771035573473192880253634733072004074347362319882093473801430000034739024041000347176130000003471418572098347139380000003471262880800347118860000003471021679785643471007199064434709326085746347079160371663470785682871"));
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
        assertEquals(Collections.emptyList(), krakenChecksumCalculator.topNEntries(map, 0));
        assertEquals(Lists.newArrayList(Pair.of(1, 11)), krakenChecksumCalculator.topNEntries(map, 1));
        assertEquals(Lists.newArrayList(Pair.of(5, 55)), krakenChecksumCalculator.topNEntries(map.descendingMap(), 1));
        assertEquals(Lists.newArrayList(Pair.of(1, 11), Pair.of(2, 22), Pair.of(3, 33)), krakenChecksumCalculator.topNEntries(map, 3));
        assertEquals(Lists.newArrayList(Pair.of(5, 55), Pair.of(4, 44), Pair.of(3, 33)), krakenChecksumCalculator.topNEntries(map.descendingMap(), 3));
        assertEquals(Lists.newArrayList(Pair.of(1, 11), Pair.of(2, 22), Pair.of(3, 33), Pair.of(4, 44), Pair.of(5, 55)), krakenChecksumCalculator.topNEntries(map, 15));
    }

    @Test
    public void digestForNumber() {
        assertEquals("350100", krakenChecksumCalculator.digestForNumber(0.3501, 6));
        assertEquals("341000", krakenChecksumCalculator.digestForNumber(0.3410, 6));
        assertEquals("1000000", krakenChecksumCalculator.digestForNumber(0.01, 8));
        assertEquals("10000000", krakenChecksumCalculator.digestForNumber(0.10, 8));
    }

    @Test
    public void digestForLevel() {
        assertEquals("3501001000000", krakenChecksumCalculator.digestForLevel(0.3501, 0.01, 6, 8));
        assertEquals("34100010000000", krakenChecksumCalculator.digestForLevel(0.3410, 0.10, 6, 8));
    }

    @Test
    public void checksum_OrderBook_ADA_USD() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            // additional
            put(0.3375, 0.93);

            // top 10 closest to spread
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
            // top 10 closest to spread
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

            // additional
            put(0.3511, 0.78);
            put(0.3512, 0.24);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks).setCurrencyPair(CurrencyPair.ADA_USD);
        assertEquals(187053740L, krakenChecksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook_BTC_USDT1() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            // additional
            put(27815.4, 6.23609732);
            put(27814.3, 7.45609761);
            put(27813.2, 4.94609759);
            put(27812.1, 4.36609774);

            // top 10 closest to spread
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
            // top 10 closest to spread
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

            // additional
            put(27835.6, 7.23411941);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks).setCurrencyPair(CurrencyPair.BTC_USDT);
        assertEquals(213973217L, krakenChecksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook_BTC_USDT2() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            // additional
            put(27745.8, 4.23500054);
            put(27744.3, 8.99500124);

            // top 10 closest to spread
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
            // top 10 closest to spread
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

            // additional
            put(27767.8, 1.12365835);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks).setCurrencyPair(CurrencyPair.BTC_USDT);
        assertEquals(1161112910L, krakenChecksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook_XRP_USD() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            // additional
            put(0.49101, 22178.74568325);

            // top 10 closest to spread
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
            // top 10 closest to spread
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

            // additional
            put(0.49185, 245.24721224);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks).setCurrencyPair(CurrencyPair.XRP_USD);
        assertEquals(780937287L, krakenChecksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook_BTT_USD() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            // additional
            put(0.00000040, 954142204.65424);
            put(0.00000039, 254142636.45462);

            // top 10 closest to spread
            put(0.00000053, 156268235.77512);
            put(0.00000052, 11402375308.59248);
            put(0.00000051, 7632597660.90208);
            put(0.00000050, 1904217511.00922);
            put(0.00000049, 308721556.32462);
            put(0.00000048, 1130657954.04297);
            put(0.00000047, 1782094130.58575);
            put(0.00000046, 1450832100.99238);
            put(0.00000045, 169739481.23666);
            put(0.00000044, 158142650.76477);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            // top 10 closest to spread
            put(0.00000054, 605602559.60260);
            put(0.00000055, 416736124.10198);
            put(0.00000056, 2532259395.03804);
            put(0.00000057, 1351563016.79919);
            put(0.00000058, 392270066.09747);
            put(0.00000059, 3874976957.22231);
            put(0.00000060, 737854695.08793);
            put(0.00000061, 1698960867.57589);
            put(0.00000062, 198350717.48451);
            put(0.00000063, 205722941.29004);

            // additional
            put(0.00000064, 914722245.65077);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks).setCurrencyPair(new CurrencyPair(Currency.BTT, Currency.USD));
        assertEquals(1830382122L, krakenChecksumCalculator.checksum(orderBook));
    }

    @Test
    public void checksum_OrderBook_BTC_JPY() {
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            // additional
            put(3680911d, 18.78350305);
            put(3680912d, 12.56350349);

            // top 10 closest to spread
            put(3690810d, 0.00027526);
            put(3690809d, 0.08815161);
            put(3690683d, 0.02778060);
            put(3688211d, 0.34724800);
            put(3688125d, 0.00037352);
            put(3685713d, 0.01813662);
            put(3685691d, 0.65978000);
            put(3684795d, 3.08189322);
            put(3682586d, 0.00020000);
            put(3680913d, 6.16350358);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            // top 10 closest to spread
            put(3696534d, 0.34724800);
            put(3698928d, 0.00037013);
            put(3699642d, 0.65997000);
            put(3701177d, 0.44103928);
            put(3701178d, 0.05000000);
            put(3706473d, 0.01918999);
            put(3706718d, 3.07581734);
            put(3708200d, 0.00019000);
            put(3708482d, 2.17329200);
            put(3711054d, 6.15181320);

            // additional
            put(3711055d, 6.28681748);
            put(3711056d, 7.85181325);
            put(3711057d, 8.75181326);
        }};
        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks).setCurrencyPair(CurrencyPair.BTC_JPY);
        assertEquals(4095657865L, krakenChecksumCalculator.checksum(orderBook));
    }

}
