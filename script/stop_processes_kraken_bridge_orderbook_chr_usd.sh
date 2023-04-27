#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver CHR-USD 01|\
KrakenOrderBookBridgeDriver CHR-USD 02|\
KrakenOrderBookBridgeDriver CHR-USD 03|\
KrakenOrderBookBridgeDriver CHR-USD 04|\
KrakenOrderBookBridgeDriver CHR-USD 05|\
KrakenOrderBookBridgeDriver CHR-USD 06|\
KrakenOrderBookBridgeDriver CHR-USD 07|\
KrakenOrderBookBridgeDriver CHR-USD 08|\
KrakenOrderBookBridgeDriver CHR-USD 09|\
KrakenOrderBookBridgeDriver CHR-USD 10"
