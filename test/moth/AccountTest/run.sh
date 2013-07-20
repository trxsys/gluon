#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.Account

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
cd ../../..; ./x.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
