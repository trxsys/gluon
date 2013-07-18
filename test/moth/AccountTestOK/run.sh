#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.Account

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
