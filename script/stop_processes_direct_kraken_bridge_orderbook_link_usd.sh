#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
DirectKrakenOrderBookBridgeDriver LINK-USD 01|\
DirectKrakenOrderBookBridgeDriver LINK-USD 02|\
DirectKrakenOrderBookBridgeDriver LINK-USD 03|\
DirectKrakenOrderBookBridgeDriver LINK-USD 04|\
DirectKrakenOrderBookBridgeDriver LINK-USD 05|\
DirectKrakenOrderBookBridgeDriver LINK-USD 06|\
DirectKrakenOrderBookBridgeDriver LINK-USD 07|\
DirectKrakenOrderBookBridgeDriver LINK-USD 08|\
DirectKrakenOrderBookBridgeDriver LINK-USD 09|\
DirectKrakenOrderBookBridgeDriver LINK-USD 10"
