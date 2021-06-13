# syntax=docker/dockerfile:1
FROM node:16-buster-slim
RUN apt-get update

# Install tools neccessary to build the audio streaming in discord.js
RUN apt-get install build-essential --no-install-recommends --yes
RUN apt-get install python3 --no-install-recommends --yes
RUN apt-get install ffmpeg --no-install-recommends --yes
RUN apt-get install sqlite3 --yes

# Install OhMyZsh with Clean template
RUN apt-get install ca-certificates curl --no-install-recommends --yes
RUN bash -c "$(curl -L https://github.com/deluan/zsh-in-docker/releases/download/v1.1.1/zsh-in-docker.sh)" -- -t clean

WORKDIR /app