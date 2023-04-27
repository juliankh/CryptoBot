#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver MXC-USD 01|\
KrakenOrderBookBridgeDriver MXC-USD 02|\
KrakenOrderBookBridgeDriver MXC-USD 03|\
KrakenOrderBookBridgeDriver MXC-USD 04|\
KrakenOrderBookBridgeDriver MXC-USD 05|\
KrakenOrderBookBridgeDriver MXC-USD 06|\
KrakenOrderBookBridgeDriver MXC-USD 07|\
KrakenOrderBookBridgeDriver MXC-USD 08|\
KrakenOrderBookBridgeDriver MXC-USD 09|\
KrakenOrderBookBridgeDriver MXC-USD 10"
