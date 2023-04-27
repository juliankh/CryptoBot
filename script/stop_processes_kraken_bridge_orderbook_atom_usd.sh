#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver ATOM-USD 01|\
KrakenOrderBookBridgeDriver ATOM-USD 02|\
KrakenOrderBookBridgeDriver ATOM-USD 03|\
KrakenOrderBookBridgeDriver ATOM-USD 04|\
KrakenOrderBookBridgeDriver ATOM-USD 05|\
KrakenOrderBookBridgeDriver ATOM-USD 06|\
KrakenOrderBookBridgeDriver ATOM-USD 07|\
KrakenOrderBookBridgeDriver ATOM-USD 08|\
KrakenOrderBookBridgeDriver ATOM-USD 09|\
KrakenOrderBookBridgeDriver ATOM-USD 10"
