package com.cb.common;

import java.util.function.Consumer;

public class BufferAggregator {

    private StringBuilder sb = new StringBuilder();

    public synchronized void process(CharSequence data, boolean last, Consumer<String> consumer) {
        sb.append(data);
        if (last) {
            consumer.accept(sb.toString());
            reset();
        }
    }

    public void reset() {
        sb = new StringBuilder();
    }

}
