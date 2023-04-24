#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver BTC-USDT 1|\
KrakenOrderBookBridgeDriver BTC-USDT 2|\
KrakenOrderBookBridgeDriver BTC-USDT 3|\
KrakenOrderBookBridgeDriver BTC-USDT 4|\
KrakenOrderBookBridgeDriver BTC-USDT 5|\
KrakenOrderBookBridgeDriver BTC-USDT 6|\
KrakenOrderBookBridgeDriver BTC-USDT 7|\
KrakenOrderBookBridgeDriver BTC-USDT 8|\
KrakenOrderBookBridgeDriver BTC-USDT 9|\
KrakenOrderBookBridgeDriver BTC-USDT 10|\
KrakenOrderBookPersisterDriver"
