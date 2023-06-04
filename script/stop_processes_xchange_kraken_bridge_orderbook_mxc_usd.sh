#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
XchangeKrakenOrderBookBridgeDriver MXC-USD 01|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 02|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 03|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 04|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 05|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 06|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 07|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 08|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 09|\
XchangeKrakenOrderBookBridgeDriver MXC-USD 10"
