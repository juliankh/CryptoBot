#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver LINK-USD 01|\
KrakenOrderBookBridgeDriver LINK-USD 02|\
KrakenOrderBookBridgeDriver LINK-USD 03|\
KrakenOrderBookBridgeDriver LINK-USD 04|\
KrakenOrderBookBridgeDriver LINK-USD 05|\
KrakenOrderBookBridgeDriver LINK-USD 06|\
KrakenOrderBookBridgeDriver LINK-USD 07|\
KrakenOrderBookBridgeDriver LINK-USD 08|\
KrakenOrderBookBridgeDriver LINK-USD 09|\
KrakenOrderBookBridgeDriver LINK-USD 10"
