package com.cb.processor;

import com.cb.common.JsonSerializer;
import com.cb.common.util.GeneralUtils;
import com.cb.common.util.NumberUtils;
import com.cb.exception.ChecksumException;
import com.cb.model.CbOrderBook;
import com.cb.processor.checksum.ChecksumCalculator;
import com.cb.processor.checksum.ChecksumVerifier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.NavigableMap;

@Slf4j
@Getter
public class SnapshotMaintainer {

    @Inject
    private ChecksumVerifier checksumVerifier;

    @Inject
    private JsonSerializer jsonSerializer;

    private CbOrderBook previousSnapshot;
    private CbOrderBook snapshot;
    private int depth;

    public void initialize(int depth, ChecksumCalculator checksumCalculator) {
        this.depth = depth;
        checksumVerifier.initialize(checksumCalculator);
    }

    public void setSnapshot(CbOrderBook snapshot) {
        setSnapshot(snapshot, true);
    }

    public void setSnapshot(CbOrderBook snapshot, boolean verifyChecksum) {
        this.previousSnapshot = this.snapshot;
        this.snapshot = snapshot;
        verifyChecksumIfNecessary(snapshot, verifyChecksum);
    }

    public void verifyChecksumIfNecessary(CbOrderBook snapshot, boolean verifyChecksum) {
        if (verifyChecksum) {
            Long derivedChecksum = checksumVerifier.confirmChecksum(snapshot);
            if (derivedChecksum != null) {
                // derivedChecksum will be non-null only if it doesn't match the one in the snapshot
                String briefMessage = "Checksum derived [" + derivedChecksum + "] is different from the one provided in the snapshot [" + snapshot.getChecksum() + "]";
                String previousSnapshotJson = jsonSerializer.serializeToJson(previousSnapshot);
                String latestSnapshotJson = jsonSerializer.serializeToJson(snapshot);
                String fullMessage = briefMessage + ":\n\tPrevious Snapshot: " + previousSnapshotJson + "\n\tLatest Snapshot: " + latestSnapshotJson;
                throw new ChecksumException(fullMessage);
            }
        }
    }

    public List<CbOrderBook> updateAndGetLatestSnapshots(List<CbOrderBook> updates, boolean verifyChecksum) {
        return updates.stream().map(update -> applyUpdateAndGetLatestCopy(update, verifyChecksum)).toList();
    }

    public CbOrderBook applyUpdateAndGetLatestCopy(CbOrderBook update, boolean verifyChecksum) {
        applyUpdate(update, verifyChecksum);
        return snapshotCopy();
    }

    public void applyUpdate(CbOrderBook update, boolean verifyChecksum) {
        if (snapshot == null) {
            throw new RuntimeException("Can't process update on top of snapshot because the snapshot is null");
        }
        previousSnapshot = snapshotCopy();
        updateLevels(snapshot.getBids(), update.getBids(), true);
        updateLevels(snapshot.getAsks(), update.getAsks(), false);
        snapshot.setExchangeDatetime(update.getExchangeDatetime())
                .setExchangeDate(update.getExchangeDate())
                .setReceivedMicros(update.getReceivedMicros())
                .setChecksum(update.getChecksum());
        verifyChecksumIfNecessary(snapshot, verifyChecksum);
    }

    public void updateLevels(NavigableMap<Double, Double> levels, NavigableMap<Double, Double> updates, boolean isBids) {
        if (MapUtils.isEmpty(levels) || MapUtils.isEmpty(updates)) {
            return;
        }
        levels.putAll(updates);
        pruneMap(isBids ? levels : levels.descendingMap()); // TODO: in live context, confirm if this ever results in having num of levels < depth (if Kraken sends a 0-qty update, will it also have new non-0 levels in order to make the final orderbook have the depth-num of levels?)
    }

    public void pruneMap(NavigableMap<Double, Double> map) {
        map.entrySet().removeIf(entry -> NumberUtils.equals(entry.getValue(), 0.0));
        GeneralUtils.pruneNavigableMap(map, depth);
    }

    public CbOrderBook snapshotCopy() {
        try {
            String json = jsonSerializer.serializeToJson(snapshot);
            return jsonSerializer.deserializeFromJson(json, CbOrderBook.class);
        } catch (Exception e) {
            throw new RuntimeException("Problem doing deep-copy of CbOrderBook", e);
        }
    }

}
