#!/bin/bash
pkill -9 Scalabot
screen -d -m bash -c "./xsbt.sh 'run-main net.scalabot.Scalabot' > log.txt"
tail -F log.txt
