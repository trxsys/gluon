#! /bin/bash

package=test.validation.`basename "$(pwd)"`
mainClass=$package.Elevator
module=$package.Controls

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
#./x.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
