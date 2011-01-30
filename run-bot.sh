#!/bin/bash
screen -d -m bash -c "./sbt.sh run > log.txt"
tail -F log.txt
