package com.cb.processor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BufferProcessorTest {

    @Mock
    private JsonProcessor jsonProcessor;

    @InjectMocks
    private BufferProcessor bufferProcessor;

    @Before
    public void beforeEachTest() {
        reset(jsonProcessor);
    }

    @Test
    public void process() {
        bufferProcessor.process("One", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("One");

        bufferProcessor.process("Two", false, jsonProcessor::process);
        bufferProcessor.process("Three", false, jsonProcessor::process);
        bufferProcessor.process("Four", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("TwoThreeFour");

        bufferProcessor.process("Five", false, jsonProcessor::process);
        bufferProcessor.process("Six", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("FiveSix");

        bufferProcessor.process("Seven", true, jsonProcessor::process);
        verify(jsonProcessor, times(1)).process("Seven");
    }

}
