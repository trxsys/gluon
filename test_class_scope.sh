#! /bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: `basename $0` <test>"
    exit 1
fi

./gluon.sh --class-scope --classpath test/target/classes/ --module "test.simple.$1.Module" "test.simple.$1.Main"
