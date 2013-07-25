#! /bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: `basename $0` <test>"
    exit 1
fi

./gluon.sh --classpath . --module "test.simple.$1.Module" "test.simple.$1.Main"

