#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.Counter

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
