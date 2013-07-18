#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Local
module=$package.Cell

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
