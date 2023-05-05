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

import java.math.BigDecimal;
import java.util.List;

import static com.cb.module.BindingName.JMS_KRAKEN_ORDERBOOK_SNAPSHOT_EXCHANGE;
import static com.cb.module.BindingName.JMS_KRAKEN_ORDERBOOK_SNAPSHOT_QUEUE;

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
        log.trace("Received book with {} bids and {} asks", orderBook.getBids().size(), orderBook.getAsks().size());
        if (CollectionUtils.isNotEmpty(orderBook.getBids()) && CollectionUtils.isNotEmpty(orderBook.getAsks())) {
            BigDecimal bestBid = orderBook.getBids().iterator().next().getLimitPrice();
            BigDecimal bestAsk = orderBook.getAsks().iterator().next().getLimitPrice();
            if (bestBid.compareTo(bestAsk) > 0) {
                log.warn("Crossed {} book, best bid {}, best ask {}", currencyPair, bestBid, bestAsk);
            }
            batchProcessor.process(
                    new KrakenOrderBook(process, TimeUtils.currentNanos(), orderBook),
                    (List<KrakenOrderBook> orderbooks) -> new KrakenOrderBookBatch(currencyPair, orderbooks),
                    jmsPublisher::publish);
        } else {
            log.warn("Received book with either empty {} bids or empty {} asks", orderBook.getBids().size(), orderBook.getAsks().size());
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        jmsPublisher.cleanup();
    }

}
