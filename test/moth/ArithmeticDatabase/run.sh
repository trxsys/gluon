#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.Table

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
