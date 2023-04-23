#!/bin/bash

if [ -f ~/.bash_profile ]; then
    source ~/.bash_profile
fi

COMMAND_PROCESSES_RUNNING="ps -ef | grep -i java | grep -v grep | grep CryptoBot | grep jar-with-dependencies"
