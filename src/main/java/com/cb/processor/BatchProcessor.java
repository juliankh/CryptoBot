package com.cb.processor;

import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class BatchProcessor<T,P> {

    private final int batchSize;

    private List<T> batch = new ArrayList<>();
    private Instant batchStart;
    private int numBatchesProcessed = 0;

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
            log.debug("Batch [" + numBatchesProcessed + "] of [" + batch.size() + " of " + data.getClass().getSimpleName() + "] took [" + TimeUtils.durationMessage(batchStart) + "] at rate of [" + NumberUtils.NUMBER_FORMAT.format(receiveRate) + "/sec] to aggregate and [" + TimeUtils.durationMessage(persistStart) + "] at a rate of [" + NumberUtils.NUMBER_FORMAT.format(processRate) + " items/sec] to process");
            batch = new ArrayList<>();
            batchStart = null;
        }
    }

}
