package com.cb.processor.checksum;

import com.cb.model.CbOrderBook;

public interface ChecksumCalculator {

    long checksum(CbOrderBook orderBook);

}
