#!/bin/bash

source ~/.crypto_bot_profile

# TODO: add to cron
${CRYPTO_BOT_BIN_SCRIPT_DIR}/common_driver_runner.sh com.cb.driver.admin.JmsQueueTrackerDriver jms_queue_tracker
