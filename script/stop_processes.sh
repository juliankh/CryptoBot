#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./stop_processes_kraken_bridge_orderbook_btc_usdt.sh &
./stop_processes_kraken_bridge_orderbook_atom_usd.sh &
./stop_processes_kraken_bridge_orderbook_mxc_usd.sh &

./common_stop_processes.sh "\
KrakenOrderBookPersisterDriver"
