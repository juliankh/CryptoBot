package com.cb.processor;

import com.cb.common.BatchProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BatchProcessorTest {

    @Mock
    private Function<List<Integer>, String> function;

    @Mock
    private Consumer<String> consumer;

    @InjectMocks
    private BatchProcessor<Integer, String> batchProcessor;

    @Test
    public void process() {
        batchProcessor.initialize(3);

        batchProcessor.process(1, function, consumer);
        verify(function, never()).apply(any());
        verify(consumer, never()).accept(any());

        batchProcessor.process(2, function, consumer);
        verify(function, never()).apply(any());
        verify(consumer, never()).accept(any());

        batchProcessor.process(3, function, consumer);
        verify(function, times(1)).apply(any());
        verify(consumer, times(1)).accept(any());
    }

}
