package com.cb.model;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Spread implements Serializable {

    private Map.Entry<Double, Double> bid;
    private Map.Entry<Double, Double> ask;

    public double bidPrice() {
        return bid.getKey();
    }

    public double bidVolume() {
        return bid.getValue();
    }

    public double askPrice() {
        return ask.getKey();
    }

    public double askVolume() {
        return ask.getValue();
    }

}
