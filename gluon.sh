#!/bin/bash

java -Xmx5048m -ea \
    -cp bin:lib/soot-2.5.0.jar:lib/java-getopt-1.0.14.jar \
    gluon.Main $* | grep --line-buffered -v '^Transforming ' \
    | egrep --line-buffered -v '^\[Spark|Call Graph\] '
