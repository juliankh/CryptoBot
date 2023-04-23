#!/bin/bash

source ~/.crypto_bot_profile

${CRYPTO_BOT_BIN_SCRIPT_DIR}/common_driver_runner.sh com.cb.driver.kraken.KrakenOrderBookBridgeDriver kraken_bridge_orderbook_$1_$2 $1 $2
