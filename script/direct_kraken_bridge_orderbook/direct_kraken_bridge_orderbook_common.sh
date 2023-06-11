#!/bin/bash

source ~/.crypto_bot_profile

${CRYPTO_BOT_BIN_SCRIPT_DIR}/common_driver_runner.sh "DirectKrakenOrderBookBridgeDriver $1 $2" direct_kraken_bridge_orderbook_$1_$2 com.cb.driver.kraken.DirectKrakenOrderBookBridgeDriver "$1 $2"
