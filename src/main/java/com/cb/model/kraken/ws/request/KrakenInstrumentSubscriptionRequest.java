package com.cb.model.kraken.ws.request;

import com.cb.common.ObjectConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenInstrumentSubscriptionRequest {

    private String method = "subscribe";
    private Integer req_id;
    private KrakenInstrumentSubscriptionRequestParams params = new KrakenInstrumentSubscriptionRequestParams();

    public static void main(String[] args) throws JsonProcessingException {
        KrakenInstrumentSubscriptionRequest subscription = new KrakenInstrumentSubscriptionRequest().setReq_id(2746);
        System.out.println(ObjectConverter.OBJECT_MAPPER.writer().writeValueAsString(subscription));
    }

}
