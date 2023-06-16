package com.cb.processor;

import com.cb.common.JsonSerializer;
import com.cb.common.util.TimeUtils;
import com.cb.model.CbOrderBook;
import com.cb.model.DataOrigin;
import com.cb.processor.checksum.ChecksumCalculator;
import com.cb.processor.checksum.ChecksumVerifier;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SnapshotMaintainerTest {

    @Mock
    private ChecksumVerifier checksumVerifier;

    @Mock
    private ChecksumCalculator checksumCalculator;

    @Spy
    private JsonSerializer jsonSerializer;

    @InjectMocks
    private SnapshotMaintainer snapshotMaintainer;

    @BeforeEach
    public void beforeEachTest() {
        reset(checksumVerifier);
        reset(checksumCalculator);
    }

    @Test
    public void setSnapshot_setSnapshot() {
        // confirm that before doing anything, the snapshot is null
        assertNull(snapshotMaintainer.getSnapshot());

        // setup - initial update
        CbOrderBook snapshot = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(TimeUtils.instant(1999, Month.APRIL, 10, 7, 23, 45))
                .setExchangeDate(LocalDate.now().minusDays(5))
                .setReceivedMicros(456123)
                .setBids(new TreeMap<>(){{
                    put(1.1, 11.1);
                    put(2.2, 22.2);
                    put(3.3, 33.3);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.5, 55.5);
                    put(6.6, 66.6);
                    put(7.7, 77.7);
                }});

        // setup - subsequent update
        Instant updateExchangeDatetime = TimeUtils.instant(2021, Month.JANUARY, 18, 13, 5, 11);
        LocalDate updateExchangeDate = LocalDate.now();
        long updateReceivedMicros = 23456687;
        CbOrderBook update = new CbOrderBook()
                .setSnapshot(false)
                .setExchangeDatetime(updateExchangeDatetime)
                .setExchangeDate(updateExchangeDate)
                .setReceivedMicros(updateReceivedMicros)
                .setBids(new TreeMap<>(){{
                    put(0.5, 0.555);
                    put(1.1, 111.1);
                    put(3.3, 0.0);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(5.5, 0.0);
                    put(7.7, 77.7);
                    put(8.8, 88.8);
                }});

        // set initial snapshot
        snapshotMaintainer.setSnapshot(snapshot, false);

        // verify that initial snapshot is set as expected, and that previousSnapshot is null
        assertSame(snapshot, snapshotMaintainer.getSnapshot());
        assertNull(snapshotMaintainer.getPreviousSnapshot());

        // apply update
        snapshot = snapshotMaintainer.snapshotCopy(); // keep a seaparte copy of the original snapshot, because otherwise the update will change it, and it won't be the same as what you originally set
        snapshotMaintainer.applyUpdate(update, false);

        // verify that update was processed such that the pre-update snapshot was saved to previousSnapshot and the new latest snapshot has the update applied
        assertEquals(snapshot, snapshotMaintainer.getPreviousSnapshot());
        assertNotEquals(snapshotMaintainer.getPreviousSnapshot(), snapshotMaintainer.getSnapshot()); // check that the new latest snapshot is different from the saved previousSnapshot
    }

    @Test
    public void updateLevels_Empty() {
        // setup
        NavigableMap<Double, Double> levels = Collections.emptyNavigableMap();
        NavigableMap<Double, Double> updates = new TreeMap<>(){{
            put(2.2, 222.2);
            put(4.4, 444.4);
        }};
        snapshotMaintainer.initialize(3, checksumCalculator);

        // engage test
        snapshotMaintainer.updateLevels(levels, updates, true);

        // verify
        assertEquals(Collections.emptyNavigableMap(), levels);
    }

    @Test
    public void updateLevels_Bids() {
        // setup
        NavigableMap<Double, Double> levels = new TreeMap<>(){{
            put(1.1, 11.1);
            put(2.2, 22.2);
            put(3.3, 33.3);
        }};
        NavigableMap<Double, Double> updates = new TreeMap<>(){{
            put(2.2, 222.2);
            put(4.4, 444.4);
        }};
        snapshotMaintainer.initialize(3, checksumCalculator);

        // engage test
        snapshotMaintainer.updateLevels(levels, updates, true);

        // verify
        NavigableMap<Double, Double> expected = new TreeMap<>(){{
            put(2.2, 222.2);
            put(3.3, 33.3);
            put(4.4, 444.4);
        }};
        assertEquals(expected, levels);
    }

    @Test
    public void updateLevels_Asks() {
        // setup
        NavigableMap<Double, Double> levels = new TreeMap<>(){{
            put(1.1, 11.1);
            put(2.2, 22.2);
            put(3.3, 33.3);
        }};
        NavigableMap<Double, Double> updates = new TreeMap<>(){{
            put(2.2, 222.2);
            put(4.4, 444.4);
        }};
        snapshotMaintainer.initialize(3, checksumCalculator);

        // engage test
        snapshotMaintainer.updateLevels(levels, updates, false);

        // verify
        NavigableMap<Double, Double> expected = new TreeMap<>(){{
            put(1.1, 11.1);
            put(2.2, 222.2);
            put(3.3, 33.3);
        }};
        assertEquals(expected, levels);
    }

    @Test
    public void applyUpdate() {
        // setup
        CbOrderBook snapshot = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(TimeUtils.instant(1999, Month.APRIL, 10, 7, 23, 45))
                .setExchangeDate(LocalDate.now().minusDays(5))
                .setReceivedMicros(456123)
                .setBids(new TreeMap<>(){{
                    put(1.1, 11.1);
                    put(2.2, 22.2);
                    put(3.3, 33.3);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.5, 55.5);
                    put(6.6, 66.6);
                    put(7.7, 77.7);
                }})
                .setChecksum(123123);
        snapshotMaintainer.setSnapshot(snapshot, false);
        snapshotMaintainer.initialize(3, checksumCalculator);

        Instant updateExchangeDatetime = TimeUtils.instant(2021, Month.JANUARY, 18, 13, 5, 11);
        LocalDate updateExchangeDate = LocalDate.now();
        long updateReceivedMicros = 23456687;
        long newChecksum = 8765432;
        CbOrderBook update = new CbOrderBook()
                .setSnapshot(false)
                .setExchangeDatetime(updateExchangeDatetime)
                .setExchangeDate(updateExchangeDate)
                .setReceivedMicros(updateReceivedMicros)
                .setBids(new TreeMap<>(){{
                    put(0.5, 0.555);
                    put(1.1, 111.1);
                    put(3.3, 0.0);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(5.5, 0.0);
                    put(7.7, 77.7);
                    put(8.8, 88.8);
                }})
                .setChecksum(newChecksum);

        // engage test
        snapshotMaintainer.applyUpdate(update, false);

        // verify
        CbOrderBook expected = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(updateExchangeDatetime)
                .setExchangeDate(updateExchangeDate)
                .setReceivedMicros(updateReceivedMicros)
                .setBids(new TreeMap<>(){{
                    put(1.1, 111.1);
                    put(2.2, 22.2);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(6.6, 66.6);
                    put(7.7, 77.7);
                }})
                .setChecksum(newChecksum);
        assertEquals(expected, snapshotMaintainer.getSnapshot());
    }

    @Test
    public void pruneMap_0ValueEntriesPrunedEvenIfUnderDepthLimit() {
        // setup
        NavigableMap<Double, Double> map = new TreeMap<>(){{
            put(4.4, 0.0);
            put(5.5, 55.5);
            put(6.6, 66.6);
            put(7.7, 77.7);
            put(8.8, 88.8);
        }};
        snapshotMaintainer.initialize(7, checksumCalculator); // way more then the size of the map (to ensure that the 0-value entries get pruned anyway)

        // engage test
        snapshotMaintainer.pruneMap(map);

        // verify
        NavigableMap<Double, Double> expected = new TreeMap<>(){{
            put(5.5, 55.5);
            put(6.6, 66.6);
            put(7.7, 77.7);
            put(8.8, 88.8);
        }};
        assertEquals(expected, map);
    }

    @Test
    public void pruneMap() {
        // setup
        NavigableMap<Double, Double> map = new TreeMap<>(){{
            put(4.4, 0.0);
            put(5.5, 55.5);
            put(6.6, 66.6);
            put(7.7, 77.7);
            put(8.8, 88.8);
        }};
        snapshotMaintainer.initialize(3, checksumCalculator);

        // engage test
        snapshotMaintainer.pruneMap(map);

        // verify
        NavigableMap<Double, Double> expected = new TreeMap<>(){{
            put(6.6, 66.6);
            put(7.7, 77.7);
            put(8.8, 88.8);
        }};
        assertEquals(expected, map);
    }

    @Test
    public void updateAndGetLatestSnapshots() {
        // setup
        CbOrderBook initialSnapshot = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(TimeUtils.instant(1999, Month.APRIL, 10, 7, 23, 45))
                .setExchangeDate(LocalDate.now().minusDays(5))
                .setReceivedMicros(456123)
                .setBids(new TreeMap<>(){{
                    put(1.1, 11.1);
                    put(2.2, 22.2);
                    put(3.3, 33.3);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.5, 55.5);
                    put(6.6, 66.6);
                    put(7.7, 77.7);
                }});
        snapshotMaintainer.setSnapshot(initialSnapshot, false);
        snapshotMaintainer.initialize(3, checksumCalculator);

        Instant updateExchangeDatetime1 = TimeUtils.instant(2021, Month.JANUARY, 18, 13, 5, 11);
        LocalDate updateExchangeDate1 = LocalDate.now().minusDays(5);
        long updateReceivedMicros1 = 23456687;
        CbOrderBook update1 = new CbOrderBook()
                .setSnapshot(false)
                .setExchangeDatetime(updateExchangeDatetime1)
                .setExchangeDate(updateExchangeDate1)
                .setReceivedMicros(updateReceivedMicros1)
                .setBids(new TreeMap<>(){{
                    put(0.5, 0.555);
                    put(1.1, 111.1);
                    put(3.3, 0.0);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(5.5, 0.0);
                    put(7.7, 77.7);
                    put(8.8, 88.8);
                }});

        Instant updateExchangeDatetime2 = TimeUtils.instant(2021, Month.JANUARY, 18, 13, 5, 12);
        LocalDate updateExchangeDate2 = LocalDate.now().minusDays(4);
        long updateReceivedMicros2 = 356789;
        CbOrderBook update2 = new CbOrderBook()
                .setSnapshot(false)
                .setExchangeDatetime(updateExchangeDatetime2)
                .setExchangeDate(updateExchangeDate2)
                .setReceivedMicros(updateReceivedMicros2)
                .setBids(new TreeMap<>(){{
                    put(3.3, 3.1);
                }})
                .setAsks(new TreeMap<>(){{
                    put(7.1, 7.11);
                }});

        Instant updateExchangeDatetime3 = TimeUtils.instant(2021, Month.JANUARY, 18, 13, 5, 13);
        LocalDate updateExchangeDate3 = LocalDate.now().minusDays(3);
        long updateReceivedMicros3 = 74589612;
        CbOrderBook update3 = new CbOrderBook()
                .setSnapshot(false)
                .setExchangeDatetime(updateExchangeDatetime3)
                .setExchangeDate(updateExchangeDate3)
                .setReceivedMicros(updateReceivedMicros3)
                .setBids(new TreeMap<>(){{
                    put(3.3, 0.0);
                }});

        Instant updateExchangeDatetime4 = TimeUtils.instant(2021, Month.JANUARY, 18, 13, 5, 14);
        LocalDate updateExchangeDate4 = LocalDate.now().minusDays(2);
        long updateReceivedMicros4 = 951456;
        CbOrderBook update4 = new CbOrderBook()
                .setSnapshot(false)
                .setExchangeDatetime(updateExchangeDatetime4)
                .setExchangeDate(updateExchangeDate4)
                .setReceivedMicros(updateReceivedMicros4)
                .setAsks(new TreeMap<>(){{
                    put(6.6, 0.0);
                    put(7.1, 2.356);
                }});

        List<CbOrderBook> updates = Lists.newArrayList(update1, update2, update3, update4);

        // engage test
        List<CbOrderBook> result = snapshotMaintainer.updateAndGetLatestSnapshots(updates, false);

        // verify
        CbOrderBook expectedSnapshot1 = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(updateExchangeDatetime1)
                .setExchangeDate(updateExchangeDate1)
                .setReceivedMicros(updateReceivedMicros1)
                .setBids(new TreeMap<>(){{
                    put(1.1, 111.1);
                    put(2.2, 22.2);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(6.6, 66.6);
                    put(7.7, 77.7);
                }});

        CbOrderBook expectedSnapshot2 = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(updateExchangeDatetime2)
                .setExchangeDate(updateExchangeDate2)
                .setReceivedMicros(updateReceivedMicros2)
                .setBids(new TreeMap<>(){{
                    put(2.2, 22.2);
                    put(3.3, 3.1);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(6.6, 66.6);
                    put(7.1, 7.11);
                }});

        CbOrderBook expectedSnapshot3 = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(updateExchangeDatetime3)
                .setExchangeDate(updateExchangeDate3)
                .setReceivedMicros(updateReceivedMicros3)
                .setBids(new TreeMap<>(){{
                    put(2.2, 22.2);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(6.6, 66.6);
                    put(7.1, 7.11);
                }});

        CbOrderBook expectedSnapshot4 = new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(updateExchangeDatetime4)
                .setExchangeDate(updateExchangeDate4)
                .setReceivedMicros(updateReceivedMicros4)
                .setBids(new TreeMap<>(){{
                    put(2.2, 22.2);
                    put(4.4, 444.4);
                }})
                .setAsks(new TreeMap<>(){{
                    put(5.4, 54.123);
                    put(7.1, 2.356);
                }});

        List<CbOrderBook> expected = Lists.newArrayList(expectedSnapshot1, expectedSnapshot2, expectedSnapshot3, expectedSnapshot4);

        assertEquals(expected, result); // verify the list of snapshots returned from the method being tested
        assertEquals(expectedSnapshot4, snapshotMaintainer.getSnapshot()); // verify the latest snapshot within the maintainer
    }

    @Test
    public void snapshotCopy() {
        // setup
        boolean isSnapshot = true;
        ZoneId zoneId = ZoneId.systemDefault();
        Instant exchangeDatetime = TimeUtils.instant(1994, Month.JULY, 23, 17, 45, 30, zoneId);
        LocalDate exchangeDate = LocalDate.ofInstant(exchangeDatetime, zoneId);
        long receivedMicros = 2398472356L;
        TreeMap<Double, Double> bids = new TreeMap<>(){{
            put(1.1, 11.1);
            put(2.2, 22.2);
            put(3.3, 33.3);
        }};
        TreeMap<Double, Double> asks = new TreeMap<>(){{
            put(111.1, 1_111.1);
            put(222.2, 2_222.2);
            put(333.3, 3_333.3);
        }};
        String misc = DataOrigin.DIRECT_KRAKEN.name();
        CbOrderBook original = new CbOrderBook()
                .setSnapshot(isSnapshot)
                .setExchangeDate(exchangeDate)
                .setExchangeDatetime(exchangeDatetime)
                .setReceivedMicros(receivedMicros)
                .setBids(bids)
                .setAsks(asks)
                .setMisc(misc);
        snapshotMaintainer.setSnapshot(original, false);

        // engage test
        CbOrderBook copy = snapshotMaintainer.snapshotCopy();

        // verify
        assertNotSame(original, copy);
        assertNotSame(original.getExchangeDatetime(), copy.getExchangeDatetime());
        assertNotSame(original.getExchangeDate(), copy.getExchangeDate());
        assertNotSame(original.getReceivedMicros(), copy.getReceivedMicros());
        assertMapNotSame(original.getBids(), copy.getBids());
        assertMapNotSame(original.getAsks(), copy.getAsks());

        assertEquals(original, copy);
        assertEquals(original.isSnapshot(), copy.isSnapshot());
        assertEquals(original.getExchangeDatetime(), copy.getExchangeDatetime());
        assertEquals(original.getExchangeDate(), copy.getExchangeDate());
        assertEquals(original.getReceivedMicros(), copy.getReceivedMicros());
        assertEquals(original.getBids(), copy.getBids());
        assertEquals(original.getAsks(), copy.getAsks());
        assertEquals(original.getMisc(), copy.getMisc());
    }

    @Test
    public void verifyChecksumIfNecessary_checksumNotVerified() {
        // engage and verify
        assertDoesNotThrow(() -> snapshotMaintainer.verifyChecksumIfNecessary(new CbOrderBook(), false));
    }

    @Test
    public void verifyChecksumIfNecessary_checksumVerifiedAndMatches() {
        // setup
        CbOrderBook orderBook = new CbOrderBook();
        when(checksumVerifier.confirmChecksum(any(CbOrderBook.class))).thenReturn(null);

        // engage and verify
        assertDoesNotThrow(() -> snapshotMaintainer.verifyChecksumIfNecessary(orderBook, true));
    }

    @Test
    public void verifyChecksumIfNecessary_checksumVerifiedAndDoesNotMatches() {
        // setup
        long snapshotChecksum = 87645;
        long derivedChecksumDifferent = 19284587;
        CbOrderBook orderBook = new CbOrderBook().setChecksum(snapshotChecksum);
        when(checksumVerifier.confirmChecksum(any(CbOrderBook.class))).thenReturn(derivedChecksumDifferent);

        // checksum verified and doesn't match
        RuntimeException exception = assertThrows(RuntimeException.class, () -> snapshotMaintainer.verifyChecksumIfNecessary(orderBook, true));
        assertEquals("Checksum derived [" + derivedChecksumDifferent + "] is different from the one provided in the snapshot [" + snapshotChecksum + "]", exception.getMessage());
    }

    private <K,V> void assertMapNotSame(Map<K,V> original, Map<K,V> copy) {
        assertNotSame(original, copy);
        Lists.newArrayList(original, copy).forEach(map -> map.keySet().forEach(key -> assertNotSame(original.get(key), copy.get(key))));
    }

}
