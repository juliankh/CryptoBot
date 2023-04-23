#!/bin/bash

if [ -f ~/.bash_profile ]; then
    source ~/.bash_profile
fi

COMMAND_PROCESSES_RUNNING="ps -ef | grep -i java | grep -v grep | grep -i CryptoBot"