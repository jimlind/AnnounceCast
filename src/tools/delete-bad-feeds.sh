#!/bin/bash
cd "`dirname "$0"`/../../"
node --loader ts-node/esm src/tools/delete-bad-feeds.ts