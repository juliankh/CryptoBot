package com.cb.ws.kraken.request;

import com.cb.common.ObjectConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.knowm.xchange.currency.CurrencyPair;

@Getter
@Setter
@Accessors(chain = true)
public class OrderBookSubscription {

    private String method = "subscribe";
    private Integer req_id;
    private OrderBookSubscriptionParams params;

    public static void main(String[] args) throws JsonProcessingException {
        OrderBookSubscription subscription = new OrderBookSubscription()
                .setReq_id(2746)
                .setParams(new OrderBookSubscriptionParams().setSnapshot(true).setDepth(100).setSymbol(Lists.newArrayList(CurrencyPair.BTC_USDT.toString())));
        System.out.println(ObjectConverter.OBJECT_MAPPER.writer().writeValueAsString(subscription));
    }

}
