package com.cb.processor;

import java.util.function.Consumer;

public class BufferProcessor {

    private final StringBuilder sb = new StringBuilder();

    // TODO: unit test
    public synchronized void process(CharSequence data, boolean last, Consumer<String> processor) {
        sb.append(data);
        if (last) {
            processor.accept(sb.toString());
            sb.setLength(0);
        }
    }

}
