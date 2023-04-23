#!/bin/bash

source ./common_profile.sh

${CRYPTO_BOT_BIN_SCRIPT_DIR}/common_driver_runner.sh com.cb.driver.kraken.KrakenOrderBookPersisterDriver kraken_persister_orderbook
