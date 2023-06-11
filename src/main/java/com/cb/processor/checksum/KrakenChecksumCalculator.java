package com.cb.processor.checksum;

import com.cb.model.CbOrderBook;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.plexus.util.StringUtils;
import org.knowm.xchange.currency.CurrencyPair;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Singleton
public class KrakenChecksumCalculator implements ChecksumCalculator {

    private static final int NUM_LEVELS = 10;

    private static final Checksum CHECKSUM = new CRC32();

    private Map<CurrencyPair, Pair<Integer, Integer>> precisionMap;

    public void initialize(Map<CurrencyPair, Pair<Integer, Integer>> precisionMap) {
        this.precisionMap = precisionMap;
    }

    @Override
    public long checksum(CbOrderBook orderBook) {
        Pair<Integer, Integer> precisions = precisionMap.get(orderBook.getCurrencyPair());
        int pricePrecision = precisions.getLeft();
        int quantityPrecision = precisions.getRight();
        String asksDigest = digestForLevels(orderBook.getAsks(), NUM_LEVELS, pricePrecision, quantityPrecision);
        String bidsDigest = digestForLevels(orderBook.getBids().descendingMap(), NUM_LEVELS, pricePrecision, quantityPrecision);
        String digest = asksDigest + bidsDigest;
        return checksum(digest);
    }

    public long checksum(String digest) {
        CHECKSUM.reset();
        CHECKSUM.update(digest.getBytes());
        return CHECKSUM.getValue();
    }

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
        return priceDigest + quantityDigest;
    }

    public String digestForNumber(double num, int precision) {
        BigDecimal bd = BigDecimal.valueOf(num);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return replaceDecimalAndStripLeadingZeros(bd);
    }

    public String replaceDecimalAndStripLeadingZeros(BigDecimal bd) {
        String s = bd.toPlainString();
        s = s.replace(".", "");
        return StringUtils.stripStart(s, "0");
    }

}
