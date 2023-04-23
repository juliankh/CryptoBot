#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "KrakenOrderBookBridgeDriver BTC-USDT 1|KrakenOrderBookPersisterDriver"
