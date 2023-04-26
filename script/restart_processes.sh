#!/bin/bash

source ~/.crypto_bot_profile

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

echo "processes currently running before restart"
eval "${COMMAND_PROCESSES_RUNNING}"

echo "stopping processes"
./stop_processes.sh
sleep 5
echo "stopped processes"

echo "processes currently running after stopping (there should be none or only the cron-triggered ones)"
eval "${COMMAND_PROCESSES_RUNNING} | grep -v AlertProvider | grep -v com.cb.driver.admin"

echo "starting processes"
./start_processes.sh
sleep 40
echo "started processes"

echo "currently running after restart"
eval "${COMMAND_PROCESSES_RUNNING}"
