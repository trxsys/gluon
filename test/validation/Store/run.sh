#! /bin/bash

package=test.validation.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.WorkerAux

cd ../../..; ./gluon.sh -pt --classpath . --module "$module" "$mainClass"
# ./gluon.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
