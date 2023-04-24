#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

echo "processes currently running before restart"
eval "${COMMAND_PROCESSES_RUNNING}"

echo "stopping processes"
./stop_processes.sh
echo "stopped processes"
sleep 3

echo "processes currently running after stopping (there should be none)"
eval "${COMMAND_PROCESSES_RUNNING} | grep -v AlertProvider"

echo "starting processes"
./start_processes.sh
echo "started processes"
sleep 5

echo "currently running after restart"
eval "${COMMAND_PROCESSES_RUNNING}"
