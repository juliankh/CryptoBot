package com.cb.injection.provider;

import com.google.inject.Provider;

import java.util.List;
import java.util.stream.Stream;

public class ListProvider<T> implements Provider<List<T>> {

    private final Provider<T> wrappedProvider;
    private final int num;

    public ListProvider(Provider<T> wrappedProvider, int num) {
        this.wrappedProvider = wrappedProvider;
        this.num = num;
    }

    @Override
    public List<T> get() {
        return Stream.generate(wrappedProvider::get).limit(num).toList();
    }

}
