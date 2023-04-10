package com.cb.processor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BatchProcessorTest {

    private static final BatchProcessor<Integer, String> BATCH_PROCESSOR = new BatchProcessor<>(3);

    @Mock
    private Function<List<Integer>, String> function;

    @Mock
    private Consumer<String> consumer;

    @Before
    public void beforeEachTest() {
        Mockito.reset(function);
        Mockito.reset(consumer);
    }

    @Test
    public void process() {
        BATCH_PROCESSOR.process(1, function, consumer);
        verify(function, never()).apply(any());
        verify(consumer, never()).accept(any());

        BATCH_PROCESSOR.process(2, function, consumer);
        verify(function, never()).apply(any());
        verify(consumer, never()).accept(any());

        BATCH_PROCESSOR.process(3, function, consumer);
        verify(function, times(1)).apply(any());
        verify(consumer, times(1)).accept(any());
    }

}
