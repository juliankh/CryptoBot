#!/bin/bash

if [ -f ~/.bash_profile ]; then
    source ~/.bash_profile
fi

${CRYPTO_BOT_DIR}/bin/script/kraken_bridge_orderbook_common.sh BTC-USDT 1