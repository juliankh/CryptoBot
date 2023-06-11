#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
DirectKrakenOrderBookBridgeDriver SOL-USD 01|\
DirectKrakenOrderBookBridgeDriver SOL-USD 02|\
DirectKrakenOrderBookBridgeDriver SOL-USD 03|\
DirectKrakenOrderBookBridgeDriver SOL-USD 04|\
DirectKrakenOrderBookBridgeDriver SOL-USD 05|\
DirectKrakenOrderBookBridgeDriver SOL-USD 06|\
DirectKrakenOrderBookBridgeDriver SOL-USD 07|\
DirectKrakenOrderBookBridgeDriver SOL-USD 08|\
DirectKrakenOrderBookBridgeDriver SOL-USD 09|\
DirectKrakenOrderBookBridgeDriver SOL-USD 10"
