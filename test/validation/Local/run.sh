#! /bin/bash

package=test.validation.`basename "$(pwd)"`
mainClass=$package.Local
module=$package.Cell

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
./x.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
