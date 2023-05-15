package com.cb.processor.kraken;

import com.cb.common.util.TimeUtils;
import com.cb.jms.common.JmsPublisher;
import com.cb.model.kraken.jms.KrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.cb.processor.BatchProcessor;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.util.List;

import static com.cb.injection.BindingName.JMS_KRAKEN_ORDERBOOK_SNAPSHOT_EXCHANGE;
import static com.cb.injection.BindingName.JMS_KRAKEN_ORDERBOOK_SNAPSHOT_QUEUE;

@Slf4j
public class KrakenOrderBookBridgeProcessor {

    @Inject
    private BatchProcessor<KrakenOrderBook, KrakenOrderBookBatch> batchProcessor;

    @Inject
    @Named(JMS_KRAKEN_ORDERBOOK_SNAPSHOT_EXCHANGE)
    private String jmsExchange;

    @Inject
    @Named(JMS_KRAKEN_ORDERBOOK_SNAPSHOT_QUEUE)
    private String jmsQueue;

    @Inject
    private JmsPublisher jmsPublisher;

    public void initialize(int batchSize) {
        batchProcessor.initialize(batchSize);
        jmsPublisher.initialize(jmsExchange, jmsQueue);
    }

    public void process(OrderBook orderBook, CurrencyPair currencyPair, String process) {
        if (CollectionUtils.isNotEmpty(orderBook.getBids()) && CollectionUtils.isNotEmpty(orderBook.getAsks())) {
            batchProcessor.process(
                    new KrakenOrderBook().setProcess(process).setMicroSeconds(TimeUtils.currentMicros()).setOrderBook(orderBook),
                    (List<KrakenOrderBook> orderbooks) -> new KrakenOrderBookBatch(currencyPair, orderbooks),
                    jmsPublisher::publish);
        } else {
            log.warn("Received book with either empty {} bids or empty {} asks, so ignoring it", orderBook.getBids().size(), orderBook.getAsks().size());
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        jmsPublisher.cleanup();
    }

}
