#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 01|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 02|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 03|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 04|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 05|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 06|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 07|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 08|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 09|\
XchangeKrakenOrderBookBridgeDriver BTC-USDT 10"
