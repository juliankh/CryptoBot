#!/bin/bash

source ./common_profile.sh

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

echo "processes currently running before restart"
ps -ef | grep -i java | grep -v grep | grep -i CryptoBot

echo "stopping processes"
./stop_processes.sh
echo "stopped processes"
sleep 1

echo "starting processes"
./start_processes.sh
echo "started processes"
sleep 1

echo "currently running after restart"
ps -ef | grep -i java | grep -v grep | grep -i CryptoBot

#TODO: see how to put the command that checks which processes running is put into a var
