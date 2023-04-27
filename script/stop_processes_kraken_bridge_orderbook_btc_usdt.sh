#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
KrakenOrderBookBridgeDriver BTC-USDT 01|\
KrakenOrderBookBridgeDriver BTC-USDT 02|\
KrakenOrderBookBridgeDriver BTC-USDT 03|\
KrakenOrderBookBridgeDriver BTC-USDT 04|\
KrakenOrderBookBridgeDriver BTC-USDT 05|\
KrakenOrderBookBridgeDriver BTC-USDT 06|\
KrakenOrderBookBridgeDriver BTC-USDT 07|\
KrakenOrderBookBridgeDriver BTC-USDT 08|\
KrakenOrderBookBridgeDriver BTC-USDT 09|\
KrakenOrderBookBridgeDriver BTC-USDT 10"
