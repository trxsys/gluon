#! /bin/bash

package=test.validation.`basename "$(pwd)"`
mainClass=$package.GUI
module=$package.Connection

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
./x.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
