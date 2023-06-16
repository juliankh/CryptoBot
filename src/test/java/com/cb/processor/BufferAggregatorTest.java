package com.cb.processor;


import com.cb.common.BufferAggregator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BufferAggregatorTest {

    @Mock
    private JsonProcessor jsonProcessor;

    @InjectMocks
    private BufferAggregator bufferAggregator;

    @BeforeEach
    public void beforeEachTest() {
        reset(jsonProcessor);
    }

    @Test
    public void process() {
        bufferAggregator.process("One", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("One");

        bufferAggregator.process("Two", false, jsonProcessor::process);
        bufferAggregator.process("Three", false, jsonProcessor::process);
        bufferAggregator.process("Four", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("TwoThreeFour");

        bufferAggregator.process("Five", false, jsonProcessor::process);
        bufferAggregator.process("Six", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("FiveSix");

        bufferAggregator.process("Seven", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("Seven");
    }

}
