#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Elevator
module=$package.Controls

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
