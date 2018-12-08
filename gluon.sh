#!/bin/bash

set -e

cd "$(dirname "$0")"

echo 'Compiling gluon and tests.'
sbt --warn compileTests
sbt --warn assembly
echo 'Compilation done.'

jar=$(find target -name 'gluon-assembly-*.jar' | sort | tail -1)

java -Xmx5048m -jar "$jar" "$@"
