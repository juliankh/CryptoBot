package com.cb.processor;

import com.cb.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class BatchProcessor<T> {

    private final int batchSize;

    private List<T> batch = new ArrayList<>();
    private Instant batchStart;
    private int numBatchesProcessed = 0;

    public synchronized void process(T data, Consumer<Collection<T>> processor) {
        batchStart = batchStart == null ? Instant.now() : batchStart;
        batch.add(data);
        if (batch.size() >= batchSize) {
            Instant persistStart = Instant.now();
            processor.accept(batch);
            Instant end = Instant.now();
            ++numBatchesProcessed;
            long receiveRate = TimeUtils.ratePerSecond(batchStart, end, batch.size());
            long processRate = TimeUtils.ratePerSecond(persistStart, end, batch.size());
            log.debug("Batch [" + numBatchesProcessed + "] of [" + batch.size() + " items] [" + data.getClass() + "] items took [" + TimeUtils.durationMessage(batchStart) + "] at rate of [" + receiveRate + "/sec] to aggregate and [" + TimeUtils.durationMessage(persistStart) + "] at a rate of [" + processRate + " items/sec] to process");
            batch = new ArrayList<>();
            batchStart = null;
        }
    }

}
