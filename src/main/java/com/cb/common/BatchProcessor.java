package com.cb.common;

import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class BatchProcessor<T,P> {

    private Integer batchSize; // using Integer instead of int so that if it's not set, a NullPointerException would be thrown (otherwise will quietly assume batchSize of 0, which is never correct)

    private List<T> batch = new ArrayList<>();
    private Instant batchStart;
    private int numBatchesProcessed = 0;

    public void initialize(int batchSize) {
        this.batchSize = batchSize;
    }

    public synchronized void process(T data, Function<List<T>,P> converter, Consumer<P> processor) {
        batchStart = batchStart == null ? Instant.now() : batchStart;
        batch.add(data);
        if (batch.size() >= batchSize) {
            Instant persistStart = Instant.now();
            P convertedBatch = converter.apply(batch);
            processor.accept(convertedBatch);
            Instant end = Instant.now();
            ++numBatchesProcessed;
            double receiveRate = TimeUtils.ratePerSecond(batchStart, end, batch.size());
            double processRate = TimeUtils.ratePerSecond(persistStart, end, batch.size());
            log.debug("Batch [" + numBatchesProcessed + "] of [" + batch.size() + " of " + data.getClass().getSimpleName() + "] took [" + TimeUtils.durationMessage(batchStart) + "] at rate of [" + NumberUtils.numberFormat(receiveRate) + "/sec] to aggregate and [" + TimeUtils.durationMessage(persistStart) + "] at a rate of [" + NumberUtils.numberFormat(processRate) + " items/sec] to process");
            batch = new ArrayList<>();
            batchStart = null;
        }
    }

}
