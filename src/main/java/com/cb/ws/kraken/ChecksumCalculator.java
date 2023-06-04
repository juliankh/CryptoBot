package com.cb.ws.kraken;

import com.cb.model.CbOrderBook;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.plexus.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

// TODO: once the logic of the checksum is established, then optimize it for performance
public class ChecksumCalculator {

    private static final int NUM_LEVELS = 10;
    private static final int PRICE_PRECISION = 6;
    private static final int QUANTITY_PRECISION = 8;

    private static final Checksum CHECKSUM = new CRC32();

    public long checksum(CbOrderBook orderBook) {
        String asksDigest = digestForLevels(orderBook.getAsks(), NUM_LEVELS, PRICE_PRECISION, QUANTITY_PRECISION);
        String bidsDigest = digestForLevels(orderBook.getBids().descendingMap(), NUM_LEVELS, PRICE_PRECISION, QUANTITY_PRECISION);
        String digest = asksDigest + bidsDigest;
        System.out.println(digest); // TODO: remove
        return checksum(digest);
    }

    public long checksum(String digest) {
        CHECKSUM.reset();
        CHECKSUM.update(digest.getBytes());
        return CHECKSUM.getValue();
    }

    // TODO: unit test
    public String digestForLevels(NavigableMap<Double, Double> map, int numEntries, int pricePrecision, int quantityPrecision) {
        List<Pair<Double, Double>> levels = topNEntries(map, numEntries);
        return levels.stream().map(entry -> {
            double price = entry.getKey();
            double quantity = entry.getValue();
            return digestForLevel(price, quantity, pricePrecision, quantityPrecision);
        }).collect(Collectors.joining());
    }

    public <K,V> List<Pair<K,V>> topNEntries(NavigableMap<K,V> map, int numEntries) {
        return map.entrySet().stream().limit(numEntries).map(e -> Pair.of(e.getKey(), e.getValue())).toList();
    }

    public String digestForLevel(double price, double quantity, int pricePrecision, int quantityPrecision) {
        String priceDigest = digestForNumber(price, pricePrecision);
        String quantityDigest = digestForNumber(quantity, quantityPrecision);
        System.out.println(priceDigest + " " + quantityDigest);
        return priceDigest + quantityDigest;
    }

    public String digestForNumber(double num, int precision) {
        BigDecimal bd = BigDecimal.valueOf(num);
        if (num > 1.0) {
            String s = replaceDecimalAndStripLeadingZeros(bd);
            return StringUtils.rightPad(s, precision, "0");
        }
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return replaceDecimalAndStripLeadingZeros(bd);
    }

    public String replaceDecimalAndStripLeadingZeros(BigDecimal bd) {
        String s = bd.toString();
        s = s.replace(".", "");
        return StringUtils.stripStart(s, "0");
    }

}
