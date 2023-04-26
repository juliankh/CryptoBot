#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}
./common_safety_net.sh "KrakenOrderBookBridgeDriver ATOM-USD 5"
