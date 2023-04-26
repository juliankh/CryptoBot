#!/bin/bash

source ~/.crypto_bot_profile

${CRYPTO_BOT_BIN_SCRIPT_DIR}/common_driver_runner.sh KrakenOrderBookPersisterDriver kraken_persister_orderbook com.cb.driver.kraken.KrakenOrderBookPersisterDriver
