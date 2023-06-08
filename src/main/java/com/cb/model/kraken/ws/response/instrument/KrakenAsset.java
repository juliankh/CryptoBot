package com.cb.model.kraken.ws.response.instrument;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class KrakenAsset {

    private String id;                  // Asset identifier, example: BTC
    private String status;
    private int precision;              // Maximum precision for asset ledger, balances
    private int precision_display;      // Recommended display precision
    private boolean borrowable;
    private double collateral_value;    // Valuation as margin collateral, if applicable
    private Double margin_rate;         // Interest rate to borrow the asset (optional)

}
