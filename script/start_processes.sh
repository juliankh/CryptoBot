#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./kraken_bridge_orderbook_btc_usdt_1.sh &
./kraken_bridge_orderbook_btc_usdt_2.sh &
./kraken_bridge_orderbook_btc_usdt_3.sh &
./kraken_bridge_orderbook_btc_usdt_4.sh &
./kraken_bridge_orderbook_btc_usdt_5.sh &
./kraken_bridge_orderbook_btc_usdt_6.sh &
./kraken_bridge_orderbook_btc_usdt_7.sh &
./kraken_bridge_orderbook_btc_usdt_8.sh &
./kraken_bridge_orderbook_btc_usdt_9.sh &
./kraken_bridge_orderbook_btc_usdt_10.sh &
./kraken_persister_orderbook.sh &
