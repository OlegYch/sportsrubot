#!/bin/bash
pkill -f Scalabot
screen -d -m bash -c "./xsbt.sh 'run-main net.scalabot.Scalabot' > log.txt"
tail -F log.txt
