package com.cb.processor.checksum;

import com.cb.model.CbOrderBook;

public class ChecksumVerifier {

    private ChecksumCalculator checksumCalculator;

    public void initialize(ChecksumCalculator checksumCalculator) {
        this.checksumCalculator = checksumCalculator;
    }

    public boolean checksumMatches(CbOrderBook orderBook) {
        return checksumCalculator.checksum(orderBook) == orderBook.getChecksum();
    }

}
