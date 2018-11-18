#!/bin/bash

export SBT_OPTS="-Xmx5048m"

args="$@"

sbt "run $args"
