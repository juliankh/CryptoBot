package com.cb.model.kraken.ws.response.instrument;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain=true)
public class KrakenInstrumentData {

    private List<KrakenAsset> assets;
    private List<KrakenAssetPair> pairs;

}
