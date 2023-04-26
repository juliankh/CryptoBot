#!/bin/bash

source ~/.bash_profile

start=`date +%s`

DEV_DIR=/Users/silky/projects/CryptoBot
DEV_SCRIPT_DIR=$DEV_DIR/script
DEV_TARGET_DIR=$DEV_DIR/target
JAR_FILE=CryptoBot-1.0-SNAPSHOT-jar-with-dependencies.jar
DEV_TARGET_JAR_FILE=$DEV_TARGET_DIR/$JAR_FILE

echo "================ Cleaning dev target dir $DEV_TARGET_DIR ================"
rm -rf $DEV_TARGET_DIR/*

echo "================ Changing to dev dir $DEV_DIR ================"
cd $DEV_DIR

echo "================ Building project ================"
mvn clean compile assembly:single

echo "================ Cleaning CryptoBot bin dir $CRYPTO_BOT_BIN_DIR ================"
rm -rf $CRYPTO_BOT_BIN_DIR/*

echo "================ Copying $DEV_TARGET_JAR_FILE to $CRYPTO_BOT_BIN_DIR ================"
cp $DEV_TARGET_JAR_FILE $CRYPTO_BOT_BIN_DIR

echo "================ Changing to CryptoBot bin dir $CRYPTO_BOT_BIN_DIR ================"
cd $CRYPTO_BOT_BIN_DIR

echo "================ Unjarring $JAR_FILE ================"
jar xf $JAR_FILE

echo "================ Setting required permissions ================"
chmod -R u+rx script/*
chmod -R go-rwx *

end=`date +%s`
diff=$(($end - $start))
duration="$(($diff / 60)) mins $(($diff % 60)) secs"
echo "================ Done, duration: $duration ================"
