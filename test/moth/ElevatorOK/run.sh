#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Elevator
module=$package.Controls

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
