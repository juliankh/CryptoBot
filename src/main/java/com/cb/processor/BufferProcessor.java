package com.cb.processor;

import java.util.function.Consumer;

public class BufferProcessor {

    private final StringBuilder sb = new StringBuilder();

    public synchronized void process(CharSequence data, boolean last, Consumer<String> consumer) {
        sb.append(data);
        if (last) {
            consumer.accept(sb.toString());
            sb.setLength(0);
        }
    }

}
