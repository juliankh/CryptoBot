package com.cb.processor;

public interface JsonProcessor {

    void process(String json);
    void cleanup();

}
