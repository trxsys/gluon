#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Local
module=$package.Cell

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
cd ../../..; ./x.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
