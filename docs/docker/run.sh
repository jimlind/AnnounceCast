#!/bin/bash
docker rm --force podcasts &> /dev/null
docker build -t podcasts ./resources
docker run --hostname podcasts.local --name podcasts -v `pwd`/../../:/app -it podcasts /bin/zsh