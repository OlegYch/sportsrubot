#!/bin/bash
screen -d -m bash -c "./xsbt.sh run > log.txt"
tail -F log.txt
