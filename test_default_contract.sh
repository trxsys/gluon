#! /bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: `basename $0` <test>"
    exit 1
fi

./gluon.sh --classpath test/target/classes/ --module "test.simple.$1.Module" --default-contract "test.simple.$1.Main"
