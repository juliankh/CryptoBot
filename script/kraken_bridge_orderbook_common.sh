#!/bin/bash

if [ -f ~/.bash_profile ]; then
    source ~/.bash_profile
fi

${CRYPTO_BOT_DIR}/bin/script/common_driver_runner.sh com.cb.driver.kraken.KrakenOrderBookBridgeDriver kraken_bridge_orderbook_$1_$2 $1 $2