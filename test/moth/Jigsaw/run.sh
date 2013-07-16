#! /bin/bash

mainClass=test.`basename "$(pwd)"`.ResourceStoreManager

if [ "$1" == "-pc" ]; then
    cd ../../..; ./moth.sh -a --dump process,views,dependency --cp src/ --sensorflag viewconsistency:pc --sensorflag arthoviewconsistency:pc $mainClass
else
    cd ../../..; ./moth.sh -a --dump process,views,dependency --cp src/ $mainClass
fi
