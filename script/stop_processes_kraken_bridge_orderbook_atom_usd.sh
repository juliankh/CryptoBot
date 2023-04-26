#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver ATOM-USD 1|\
KrakenOrderBookBridgeDriver ATOM-USD 2|\
KrakenOrderBookBridgeDriver ATOM-USD 3|\
KrakenOrderBookBridgeDriver ATOM-USD 4|\
KrakenOrderBookBridgeDriver ATOM-USD 5|\
KrakenOrderBookBridgeDriver ATOM-USD 6|\
KrakenOrderBookBridgeDriver ATOM-USD 7|\
KrakenOrderBookBridgeDriver ATOM-USD 8|\
KrakenOrderBookBridgeDriver ATOM-USD 9|\
KrakenOrderBookBridgeDriver ATOM-USD 10"
