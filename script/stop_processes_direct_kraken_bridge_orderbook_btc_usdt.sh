#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./common_stop_processes.sh "\
DirectKrakenOrderBookBridgeDriver BTC-USDT 01|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 02|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 03|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 04|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 05|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 06|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 07|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 08|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 09|\
DirectKrakenOrderBookBridgeDriver BTC-USDT 10"
