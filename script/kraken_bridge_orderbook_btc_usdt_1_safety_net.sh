#!/bin/bash

source ./common_profile.sh

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}
./common_safety_net.sh "KrakenOrderBookBridgeDriver BTC-USDT 1"
