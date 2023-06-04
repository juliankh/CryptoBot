#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
XchangeKrakenOrderBookBridgeDriver SOL-USD 01|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 02|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 03|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 04|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 05|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 06|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 07|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 08|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 09|\
XchangeKrakenOrderBookBridgeDriver SOL-USD 10"
