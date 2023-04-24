#!/bin/bash

source ~/.crypto_bot_profile

${CRYPTO_BOT_BIN_SCRIPT_DIR}/common_driver_runner.sh com.cb.driver.admin.JmsQueueMonitorDriver jms_queue_monitor
