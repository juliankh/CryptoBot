#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

./start_processes_xchange_kraken_bridge_orderbook_btc_usdt.sh & sleep 10
./start_processes_direct_kraken_bridge_orderbook_btc_usdt.sh & sleep 10

./start_processes_xchange_kraken_bridge_orderbook_sol_usd.sh & sleep 10
./start_processes_direct_kraken_bridge_orderbook_sol_usd.sh & sleep 10

./start_processes_xchange_kraken_bridge_orderbook_atom_usd.sh & sleep 10
./start_processes_direct_kraken_bridge_orderbook_atom_usd.sh & sleep 10

./start_processes_xchange_kraken_bridge_orderbook_link_usd.sh & sleep 10
./start_processes_direct_kraken_bridge_orderbook_link_usd.sh & sleep 10

./start_processes_xchange_kraken_bridge_orderbook_chr_usd.sh & sleep 10
./start_processes_direct_kraken_bridge_orderbook_chr_usd.sh &

# TODO: put this back
#./start_processes_xchange_kraken_bridge_orderbook_mxc_usd.sh & sleep 10
#./start_processes_direct_kraken_bridge_orderbook_mxc_usd.sh & sleep 10

