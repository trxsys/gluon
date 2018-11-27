#!/bin/bash

cd "$(dirname "$0")"

sbt assembly

jar=$(find target -name 'gluon-assembly-*.jar' | sort | tail -1)

java -Xmx5048m -jar "$jar" "$@"
