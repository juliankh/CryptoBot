package com.cb.model.kraken.ws.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DbKrakenAsset {

    private Long id;
    private String kraken_id;           // Asset identifier, example: BTC
    private String status;
    private int precision;              // Maximum precision for asset ledger, balances
    private int precision_display;      // Recommended display precision
    private boolean borrowable;
    private double collateral_value;    // Valuation as margin collateral, if applicable
    private Double margin_rate;         // Interest rate to borrow the asset (optional)
    private Timestamp created;
    private Timestamp updated;

}
