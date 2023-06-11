#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
DirectKrakenOrderBookBridgeDriver CHR-USD 01|\
DirectKrakenOrderBookBridgeDriver CHR-USD 02|\
DirectKrakenOrderBookBridgeDriver CHR-USD 03|\
DirectKrakenOrderBookBridgeDriver CHR-USD 04|\
DirectKrakenOrderBookBridgeDriver CHR-USD 05|\
DirectKrakenOrderBookBridgeDriver CHR-USD 06|\
DirectKrakenOrderBookBridgeDriver CHR-USD 07|\
DirectKrakenOrderBookBridgeDriver CHR-USD 08|\
DirectKrakenOrderBookBridgeDriver CHR-USD 09|\
DirectKrakenOrderBookBridgeDriver CHR-USD 10"
