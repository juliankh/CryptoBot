package com.cb.processor;

import com.cb.common.ObjectConverter;
import com.cb.common.util.GeneralUtils;
import com.cb.common.util.NumberUtils;
import com.cb.model.CbOrderBook;
import com.cb.ws.kraken.ChecksumCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NavigableMap;

@Getter
public class SnapshotMaintainer {

    @Inject
    private ChecksumCalculator checksumCalculator;

    private CbOrderBook snapshot;
    private int depth;

    public void initialize(int depth) {
        this.depth = depth;
    }

    public void setSnapshot(CbOrderBook snapshot) {
        setSnapshot(snapshot, true);
    }

    public void setSnapshot(CbOrderBook snapshot, boolean verifyChecksum) {
        this.snapshot = snapshot;
        if (verifyChecksum) {
            // TODO: verify checksum;
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
        updateLevels(snapshot.getBids(), update.getBids(), true);
        updateLevels(snapshot.getAsks(), update.getAsks(), false);
        snapshot.setExchangeDatetime(update.getExchangeDatetime())
                .setExchangeDate(update.getExchangeDate())
                .setReceivedMicros(update.getReceivedMicros());
        if (verifyChecksum) {
            // TODO: verify checksum
        }
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
            String json = ObjectConverter.OBJECT_MAPPER.writer().writeValueAsString(snapshot);
            return ObjectConverter.OBJECT_MAPPER.readValue(json, CbOrderBook.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Problem doing deep-copy of CbOrderBook", e);
        }
    }

    public String snapshotAgeLogMsg(Instant timeToCompareTo) {
        if (snapshot != null) {
            long secsDiff = ChronoUnit.SECONDS.between(snapshot.getExchangeDatetime(), timeToCompareTo);
            return "Latest OrderBook Snapshot was generated [" + secsDiff + "] secs ago";
        } else {
            return "Latest OrderBook Snapshot hasn't been set yet";
        }
    }

}
