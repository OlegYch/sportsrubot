#!/bin/bash
./sbt.sh reload
./sbt.sh update
screen -d -m bash -c "./sbt.sh run > log.txt"
tail -F log.txt
