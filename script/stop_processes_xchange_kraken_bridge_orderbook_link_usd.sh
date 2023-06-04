#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
XchangeKrakenOrderBookBridgeDriver LINK-USD 01|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 02|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 03|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 04|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 05|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 06|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 07|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 08|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 09|\
XchangeKrakenOrderBookBridgeDriver LINK-USD 10"
