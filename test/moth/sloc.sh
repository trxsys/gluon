#! /bin/bash

for f in $@; do
    # remove whitespace     ignore trivial lines  ignore comments
    sed 's/[\t ]//g' "$f" | egrep -v '^.{0,3}$' | egrep -v '^//'
done | wc -l

exit 0
