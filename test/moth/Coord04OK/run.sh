#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.CoordMain
module=$package.Coord

cd ../../..; ./x.sh -pt --classpath . --module "$module" "$mainClass"
cd ../../..; ./x.sh --no-grammar-opt -pt --classpath . --module "$module" "$mainClass"
