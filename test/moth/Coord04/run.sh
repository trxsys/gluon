#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.CoordMain
module=$package.Coord

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
