#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver SOL-USD 01|\
KrakenOrderBookBridgeDriver SOL-USD 02|\
KrakenOrderBookBridgeDriver SOL-USD 03|\
KrakenOrderBookBridgeDriver SOL-USD 04|\
KrakenOrderBookBridgeDriver SOL-USD 05|\
KrakenOrderBookBridgeDriver SOL-USD 06|\
KrakenOrderBookBridgeDriver SOL-USD 07|\
KrakenOrderBookBridgeDriver SOL-USD 08|\
KrakenOrderBookBridgeDriver SOL-USD 09|\
KrakenOrderBookBridgeDriver SOL-USD 10"
