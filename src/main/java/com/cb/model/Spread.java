package com.cb.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Spread {

    private final Pair<Double, Double> bid;
    private final Pair<Double, Double> ask;

    public double bidPrice() {
        return bid.getLeft();
    }

    public double bidVolume() {
        return bid.getRight();
    }

    public double askPrice() {
        return ask.getLeft();
    }

    public double askVolume() {
        return ask.getRight();
    }

}
