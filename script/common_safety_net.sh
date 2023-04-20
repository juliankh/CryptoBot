#!/bin/bash

source ./common_profile.sh

DATETIME=`date "+%Y-%m-%d %H:%M:%S"`
SAFETY_NET_LOG=${CRYPTO_BOT_LOG_DIR}/safety_nets.log

if ps -ef | grep -v grep | grep java | grep CryptoBot | grep "$1" ; then
    echo "${DATETIME} - ${1} is up, which is good" | tee -a ${SAFETY_NET_LOG}
else
    subject="${1} is DOWN !!!!!!!!!!!!!!!!!!!"
    echo "${DATETIME} - ${subject}" | tee -a ${SAFETY_NET_LOG}
    cd ${CRYPTO_BOT_BIN_DIR}
    java -cp ./CryptoBot-1.0-SNAPSHOT-jar-with-dependencies.jar com.cb.alert.AlertProvider "${subject}" "${subject}"
fi
