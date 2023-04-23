#!/bin/bash

source ./common_profile.sh

cd ${CRYPTO_BOT_BIN_SCRIPT_DIR}

CUSTOM_COMMAND_PROCESS_RUNNING="${COMMAND_PROCESSES_RUNNING} | egrep \"$1\""

if (eval "${CUSTOM_COMMAND_PROCESS_RUNNING}") ; then
    kill $(eval "${CUSTOM_COMMAND_PROCESS_RUNNING}" | awk '{print $2}')
else
    echo "Nothing to stop from: ${1}"
fi
