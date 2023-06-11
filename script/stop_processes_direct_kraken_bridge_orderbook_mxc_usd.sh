#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
DirectKrakenOrderBookBridgeDriver MXC-USD 01|\
DirectKrakenOrderBookBridgeDriver MXC-USD 02|\
DirectKrakenOrderBookBridgeDriver MXC-USD 03|\
DirectKrakenOrderBookBridgeDriver MXC-USD 04|\
DirectKrakenOrderBookBridgeDriver MXC-USD 05|\
DirectKrakenOrderBookBridgeDriver MXC-USD 06|\
DirectKrakenOrderBookBridgeDriver MXC-USD 07|\
DirectKrakenOrderBookBridgeDriver MXC-USD 08|\
DirectKrakenOrderBookBridgeDriver MXC-USD 09|\
DirectKrakenOrderBookBridgeDriver MXC-USD 10"
