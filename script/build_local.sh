#!/bin/bash

source ~/.bash_profile

DEV_DIR=/Users/silky/projects/CryptoBot
DEV_SCRIPT_DIR=$DEV_DIR/script
DEV_TARGET_DIR=$DEV_DIR/target
JAR_FILE_NAME=CryptoBot-1.0-SNAPSHOT-jar-with-dependencies.jar
DEV_TARGET_JAR_FILE_NAME=$DEV_TARGET_DIR/$JAR_FILE_NAME
CB_BIN_DIR=$CB_BOT_DIR/bin

echo "================ Changing to dev dir $DEV_DIR ================"
cd $DEV_DIR

echo "================ Building project ================"
mvn clean compile assembly:single

echo "================ Cleaning CB bin dir $CB_BIN_DIR ================"
rm -rf $CB_BIN_DIR/*

echo "================ Copying $DEV_TARGET_JAR_FILE_NAME to $CB_BIN_DIR ================"
cp $DEV_TARGET_JAR_FILE_NAME $CB_BIN_DIR

echo "================ Changing to CB bin dir $CB_BIN_DIR ================"
cd $CB_BIN_DIR

echo "================ Unjarring $JAR_FILE_NAME ================"
jar xf $JAR_FILE_NAME

echo "================ Setting required permissions on scripts ================"
chmod u+rx script/*

echo "================ Done ================"