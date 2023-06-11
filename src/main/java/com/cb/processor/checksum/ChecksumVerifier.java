package com.cb.processor.checksum;

import com.cb.model.CbOrderBook;

public class ChecksumVerifier {

    private ChecksumCalculator checksumCalculator;

    public void initialize(ChecksumCalculator checksumCalculator) {
        this.checksumCalculator = checksumCalculator;
    }

    // If checksum matches, then return null.  If checksum doesn't match, then returns the derived checksum (which doesn't match the checksum included in the snapshot)
    public Long confirmChecksum(CbOrderBook orderBook) {
        long derivedChecksum = checksumCalculator.checksum(orderBook);
        return derivedChecksum == orderBook.getChecksum() ?  null : derivedChecksum;
    }

}
