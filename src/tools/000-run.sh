#!/bin/bash
parent_directory="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
node --experimental-json-modules --loader ts-node/esm $parent_directory/$1