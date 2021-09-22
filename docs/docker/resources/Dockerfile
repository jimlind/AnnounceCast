# syntax=docker/dockerfile:1
FROM node:16-buster-slim
RUN apt-get update

# Install tools neccessary to build the audio streaming in discord.js
RUN apt-get install autoconf automake build-essential libtool python3 --no-install-recommends --yes
RUN apt-get install ffmpeg --no-install-recommends --yes
# Local database for data storage
RUN apt-get install sqlite3 --no-install-recommends --yes

# Install OhMyZsh with Clean template
RUN apt-get install ca-certificates curl --no-install-recommends --yes
RUN bash -c "$(curl -L https://github.com/deluan/zsh-in-docker/releases/download/v1.1.1/zsh-in-docker.sh)" -- -t clean

WORKDIR /app