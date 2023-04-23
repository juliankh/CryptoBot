#!/bin/bash

source ./common_profile.sh

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "KrakenOrderBookBridgeDriver BTC-USDT 1|KrakenOrderBookPersisterDriver"
