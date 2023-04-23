#!/bin/bash

source ~/.crypto_bot_profile

DATETIME=`date +%Y-%m-%d_%H-%M-%S`

if ps -ef | grep -v grep | grep java | grep CryptoBot | grep "${1}" ; then
    echo "${1} already running"
	exit 0
else
	cd $CRYPTO_BOT_BIN_DIR
	java -cp ./CryptoBot-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $3 $4 &> "${CRYPTO_BOT_LOG_DIR}/$2_$DATETIME.log"
	exit 0
fi
