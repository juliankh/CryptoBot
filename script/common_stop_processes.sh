#!/bin/bash

source ./common_profile.sh

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

#TODO: see how to put the command that checks which processes running is put into a var
if ps -ef | grep -v grep | grep java | grep CryptoBot | egrep "$1" ; then
    kill $(ps -ef | grep -v grep | grep java | grep CryptoBot | egrep "$1" | awk '{print $2}')
else
    echo "Nothing to stop from: ${1}"
fi
