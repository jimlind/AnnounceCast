#!/bin/bash
parent_directory="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
node --loader ts-node/esm $parent_directory/reset-all-posted-data.ts