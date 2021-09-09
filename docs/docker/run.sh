#!/bin/bash
parent_directory="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

docker rm --force podcasts &> /dev/null
docker build -t podcasts $parent_directory/resources
docker run --hostname podcasts.local --name podcasts -v $parent_directory/../../:/app -it podcasts /bin/zsh