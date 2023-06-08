package com.cb.model.kraken.ws.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Getter
@Setter
@Accessors(chain=true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbKrakenAssetPair {

    private Long id;
    private String symbol;                  // example: "EUR/USD"
    private String base;                    // example: "EUR"
    private String quote;                   // example: "USD"
    private String status;
    private boolean has_index;              // Whether the pair has an index available, like stop-loss triggers
    private boolean marginable;             // Whether the pair can be traded on margin
    private Double margin_initial;          // Initial margin requirement percent, if marginable (Optional)
    private Integer position_limit_long;    // Limit for long positions for marginable pairs (Optional)
    private Integer position_limit_short;   // Limit for short positions for marginable pairs (Optional)

    private double qty_min;                 // example: 0.50000000  - Minimum quantity, in base currency, for new orders
    private int qty_precision;              // example: 8           - Maximum precision used for order quantities
    private double qty_increment;           // example: 0.00000001  - Minimum quantity increment for new orders

    private int price_precision;            // example: 5           - Maximum precision used for order prices
    private double price_increment;         // example: 0.00001     - Minimum price increment for new orders

    private double cost_min;                // example: 0.50        - Minimum cost (price * qty) for new orders
    private int cost_precision;             // example: 5           - Maximum precision used for cost prices

    private Timestamp created;
    private Timestamp updated;

}
