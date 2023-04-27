#!/bin/bash

source ~/.crypto_bot_profile

DATETIME=`date +%Y-%m-%d_%H-%M-%S`

#
# $1: token to search for when checking if the process is already running (used by internal common scripts)
# $2: the log file name prefix
# $3: the class to run
# $4: the args to the class (may contain multiple args) -- OPTIONAL
#

if ps -ef | grep -v grep | grep java | grep CryptoBot | grep "$1" ; then
    echo "$1 already running"
	exit 0
else
	cd $CRYPTO_BOT_BIN_DIR
	java -cp ./CryptoBot-1.0-SNAPSHOT-jar-with-dependencies.jar $3 $4 &> "${CRYPTO_BOT_LOG_DIR}/$2_$DATETIME.log"
	exit 0
fi
