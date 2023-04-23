#!/bin/bash

source ./common_profile.sh

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./kraken_bridge_orderbook_btc_usdt_1.sh &
./kraken_persister_orderbook.sh &
