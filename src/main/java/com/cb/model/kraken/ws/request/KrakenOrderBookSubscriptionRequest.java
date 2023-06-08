package com.cb.model.kraken.ws.request;

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
public class KrakenOrderBookSubscriptionRequest {

    private String method = "subscribe";
    private Integer req_id;
    private KrakenOrderBookSubscriptionRequestParams params;

    public static void main(String[] args) throws JsonProcessingException {
        KrakenOrderBookSubscriptionRequest subscription = new KrakenOrderBookSubscriptionRequest()
                .setReq_id(2746)
                .setParams(new KrakenOrderBookSubscriptionRequestParams().setSnapshot(true).setDepth(100).setSymbol(Lists.newArrayList(CurrencyPair.BTC_USDT.toString())));
        System.out.println(ObjectConverter.OBJECT_MAPPER.writer().writeValueAsString(subscription));
    }

}
