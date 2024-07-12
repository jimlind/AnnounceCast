# Hosted Setup

This document mostly serves as a record for myself so that I don't have to try to remember everything. Maybe other people might get some value from it, but that seems extremely doubtful.

It is hosted on a small VPS on https://crunchbits.com running Ubuntu 22 LTS. VPS has 1 CPU Core, 1.5 GB RAM, 40 GB SSD Storage, and an IPv4 address. I got it from a sale thread on https://lowendtalk.com/ when my previous host went under. The VPS has plenty of power for what I need and a bonux that when I use up my allocated bandwidth for a month Crunchbits doesn't shut me down but deprioritizes my traffic. Perfect for what I need.

I tried my best to get things in the right order and have sudo in the right place, but I probably screwed up some of these directions. 🤷

## Install Ubuntu 22 System Dependency

If you are running another OS this may not directly translate but for the system I'm running this on it
takes care of what I need.

### Create New Specific Bot User

After you have finished this login as the user that you created. Here went with the username "discord."

```shell
sudo adduser discord
```

### NodeJS and NPM via NVM

I like the idea of having NodeJS and NPM installed via NVM because it gives me a lot of flexibility in the specific version of the the tools that I have installed on the server.

At the time of writing this installs NodeJS v22.3.0 and NPM v10.8.1

```shell
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc
nvm install 22
```

### SQLite v3 via APT

```shell
sudo apt install sqlite3 --no-install-recommends
```

## Install Application and Make New User Owner

```shell
cd /opt/
sudo git clone https://github.com/jimlind/AnnounceCast.git
sudo chown -R discord:discord /opt/AnnounceCast/
cd AnnounceCast
npm install
```

## Put Authorization Keys In Place

You'll do this how ever you like, but I generally open the files in vim and copy/paste as neccessary.

```shell
vim /opt/AnnounceCast/.env.development
vim /opt/AnnounceCast/.env.production
```

## Configure Application as Systemd Daemon

The config file that I have saved in the docs is of particular interest because you need to use `nvm-exec` due to how the particular versions of NodeJS and NVM are installed. Took a while for me to sort that little bit out.

```shell
sudo cp /opt/AnnounceCast/docs/hosted/announcecast.service /lib/systemd/system/
sudo systemctl daemon-reload
sudo systemctl start announcecast
```

## Publish Slash Commands

The Discord servers need to know what slash commands they should use for the bot. Run the following command to put them in place. You should see a message that says "Set 6 Application Commands" when it completes.

```shell
NODE_ENV=production node --import ./register.mjs ./src/tools/app-command/put-application-commands.ts
```

## Miscellaneous Shortcuts

```shell
# Follow local application logs
sudo tail -f /opt/AnnounceCast/log/application.log
# Follow system service logs
sudo journalctl -u announcecast.service -f
# Start system service
sudo systemctl start announcecast
# Check system service status
sudo systemctl status announcecast
# Stop system service
sudo systemctl stop announcecast
# Watch live logs from AnnounceCast
tail -f /opt/app/discord.podcasts/log/application.log
```

## Observability

### Grafana

I thought about this for a while, but eventually decided to stop thinking and take advantage of free tier of Grafana Cloud. That limits the length of time and quantity of logs they'll keep but for this project I'm well inside the limits of what they offer.

You should start with the "Get Started" documebntation here https://grafana.com/docs/alloy/latest/get-started/run/linux/.

The [config.alloy](config.alloy) file here contains mostly default configs. Values marked with `$$$$ grafana_value $$$$` need to be replace with values from your Grafana configs. The file in question in stored in `/etc/alloy/config.alloy` on a normal linux system. There are articles with some additional information about the config here:

-   https://grafana.com/docs/grafana-cloud/monitor-infrastructure/integrations/integration-reference/integration-linux-node/
-   https://www.andreacasarin.com/2024/5/4/grafana-agent-meets-alloy.html.

### Logging Permission in Grafana Alloy

The important thing is that logs are set to allow all to read all the way up the parent chain. The `/opt/` directory should be that way but we want to make sure the rest are as well.

```shell
sudo chmod a+rx /opt/AnnounceCast
sudo chmod a+rx /opt/AnnounceCast/log
sudo chmod 644 /opt/AnnounceCast/log/*
```

### Trying to Figure Out "Matches" in loki.souce.journal

Documentation is here: https://grafana.com/docs/alloy/latest/reference/components/loki.source.journal/
PR for code change (and tests) is here: https://github.com/grafana/agent/pull/2825/files

From looking at the code and the test I sorted out that the intent is that the the format is FIELD=value but I couldn't really tell what it wanted in either of those fields. Through a bunch of trial and error I was able to sort out that it wants something like the string below. It has been commited to my reference config.alloy file.

```
matches = "_SYSTEMD_UNIT=announcecast.service"
```

When I get my real blog running I need to create an article about this because it wasn't really documented anywhere.

## Development Version of the Bot

As you may have figured out from the `.env` files above but there are dev and prod versions of the bot registered with Discord. The dev version of the bot shouldn't be useful to anybody other than me. It is only online for short bursts so I can test things. None the less I want to have it documented for myself.

Use this link to authorize the dev bot on your server:  
https://discord.com/oauth2/authorize?client_id=851611029687500850&permissions=280576&scope=bot%20applications.commands

I use this command to setup dev commands for my bot:

```shell
NODE_ENV=development node --import ./register.mjs ./src/tools/app-command/put-application-commands.ts
```
