#! /bin/bash

if [ "$#" == "0" ]; then
    tests=`ls`
else
    tests=$@
fi

for f in $tests; do
    if test -d "$f" && test -x "$f/run.sh"; then
        cd $f

        echo -e "\033[32mRunning $f\033[m"
        time ./run.sh > result

        if [ $? != 0 ]; then
            echo -e "\033[31mError: Aborting\033[m"
            exit -1
        fi

        cd ..
    fi
done
