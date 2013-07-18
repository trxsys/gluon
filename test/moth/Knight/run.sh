#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.KnightMoves

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
