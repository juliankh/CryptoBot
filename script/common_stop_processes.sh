#!/bin/bash

source ~/.crypto_bot_profile

CUSTOM_COMMAND_PROCESS_RUNNING="${COMMAND_PROCESSES_RUNNING} | egrep \"$1\""

if (eval "${CUSTOM_COMMAND_PROCESS_RUNNING}") ; then
    kill $(eval "${CUSTOM_COMMAND_PROCESS_RUNNING}" | awk '{print $2}')
else
    echo "Nothing to stop from: ${1}"
fi
