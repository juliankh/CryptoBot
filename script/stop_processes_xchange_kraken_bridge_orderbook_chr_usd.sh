#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
XchangeKrakenOrderBookBridgeDriver CHR-USD 01|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 02|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 03|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 04|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 05|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 06|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 07|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 08|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 09|\
XchangeKrakenOrderBookBridgeDriver CHR-USD 10"
