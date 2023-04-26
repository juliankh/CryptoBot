#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver MXC-USD 1|\
KrakenOrderBookBridgeDriver MXC-USD 2|\
KrakenOrderBookBridgeDriver MXC-USD 3|\
KrakenOrderBookBridgeDriver MXC-USD 4|\
KrakenOrderBookBridgeDriver MXC-USD 5|\
KrakenOrderBookBridgeDriver MXC-USD 6|\
KrakenOrderBookBridgeDriver MXC-USD 7|\
KrakenOrderBookBridgeDriver MXC-USD 8|\
KrakenOrderBookBridgeDriver MXC-USD 9|\
KrakenOrderBookBridgeDriver MXC-USD 10"
