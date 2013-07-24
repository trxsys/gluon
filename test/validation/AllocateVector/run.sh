#! /bin/bash

package=test.validation.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.AllocationVector

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
./x.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
