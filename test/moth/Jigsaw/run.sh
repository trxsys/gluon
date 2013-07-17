#! /bin/bash

package=test.moth.`basename "$(pwd)"`
mainClass=$package.Main
module=$package.ResourceStoreManager

cd ../../..; ./x.sh --classpath . --module "$module" "$mainClass"
