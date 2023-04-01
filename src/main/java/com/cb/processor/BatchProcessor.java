package com.cb.processor;

import com.cb.util.CryptoUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

// TODO: make it persist directly to db, gauge limit of throughput and ideal batch size.  Then send via jms to be persisted asynchronously.
@Slf4j
@RequiredArgsConstructor
public class BatchProcessor<T> {

    private final int batchSize;

    private final List<T> batch = new ArrayList<>();

    private Instant batchStart;

    // TODO: unit test
    public synchronized void process(T data, Consumer<Collection<T>> processor) {
        batchStart = batchStart == null ? Instant.now() : batchStart;
        batch.add(data);
        if (batch.size() >= batchSize) {
            Instant persistStart = Instant.now();
            processor.accept(batch);
            log.debug("Batch of [" + batch.size() + "] [" + data.getClass() + "] items took [" + CryptoUtils.durationMessage(batchStart) + "] to aggregate and [" + CryptoUtils.durationMessage(persistStart) + "] to process");
            batch.clear();
            batchStart = null;
        }
    }

}
