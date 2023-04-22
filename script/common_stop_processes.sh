#!/bin/bash

source ./common_profile.sh

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

if ps -ef | grep -v grep | grep java | grep CryptoBot | grep "$1" ; then
    kill $(ps -ef | grep -v grep | grep java | egrep "$1" | awk '{print $2}')
else
    echo "Nothing to stop from: ${1}"
fi
