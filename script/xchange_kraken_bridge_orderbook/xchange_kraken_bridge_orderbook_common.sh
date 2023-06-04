#!/bin/bash

source ~/.crypto_bot_profile

${CRYPTO_BOT_BIN_SCRIPT_DIR}/common_driver_runner.sh "XchangeKrakenOrderBookBridgeDriver $1 $2" xchange_kraken_bridge_orderbook_$1_$2 com.cb.driver.kraken.XchangeKrakenOrderBookBridgeDriver "$1 $2"
