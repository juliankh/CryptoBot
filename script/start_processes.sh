#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./start_processes_kraken_bridge_orderbook_btc_usdt.sh &
./start_processes_kraken_bridge_orderbook_atom_usd.sh &
./start_processes_kraken_bridge_orderbook_mxc_usd.sh &
./kraken_persister_orderbook.sh &
