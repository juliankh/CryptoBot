package com.cb.processor;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BatchProcessorTest {

    private static final BatchProcessor<String> BATCH_PROCESSOR = new BatchProcessor<>(3);

    @Mock
    private Consumer<Collection<String>> function;

    @Before
    public void beforeEachTest() {
        Mockito.reset(function);
    }

    /*
    public synchronized void process(T data, Consumer<Collection<T>> processor) {
        batchStart = batchStart == null ? Instant.now() : batchStart;
        batch.add(data);
        if (batch.size() >= batchSize) {
            Instant persistStart = Instant.now();
            processor.accept(batch);
            Instant end = Instant.now();
            long receiveRate = TimeUtils.ratePerSecond(batchStart, end, batch.size());
            long processRate = TimeUtils.ratePerSecond(persistStart, end, batch.size());
            log.debug("Batch of [" + batch.size() + "] [" + data.getClass() + "] items took [" + TimeUtils.durationMessage(batchStart) + "] at rate of [" + receiveRate + "/sec] to aggregate and [" + TimeUtils.durationMessage(persistStart) + "] at a rate of [" + processRate + " items/sec] to process");
            batch.clear();
            batchStart = null;
        }
    }
     */
    @Test
    public void process() {
        BATCH_PROCESSOR.process("one", function);
        verify(function, never()).accept(any());

        BATCH_PROCESSOR.process("two", function);
        verify(function, never()).accept(any());

        BATCH_PROCESSOR.process("three", function);
        verify(function, times(1)).accept(Lists.newArrayList("one", "two", "three"));

    }

}
