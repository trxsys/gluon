#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.GUI
module=$package.Connection

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
