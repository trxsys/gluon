#! /bin/bash

package=test.validation.`basename "$(pwd)"`
mainClass=$package.Local
module=$package.Cell

cd ../../..; ./gluon.sh -pt --classpath test/target/classes/ --module "$module" "$mainClass"
# ./gluon.sh --no-grammar-opt -pt --classpath test/target/classes/ --module "$module" "$mainClass"
