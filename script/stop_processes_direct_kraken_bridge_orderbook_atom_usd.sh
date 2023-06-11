#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
DirectKrakenOrderBookBridgeDriver ATOM-USD 01|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 02|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 03|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 04|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 05|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 06|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 07|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 08|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 09|\
DirectKrakenOrderBookBridgeDriver ATOM-USD 10"
