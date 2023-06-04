#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 01|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 02|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 03|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 04|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 05|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 06|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 07|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 08|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 09|\
XchangeKrakenOrderBookBridgeDriver ATOM-USD 10"
