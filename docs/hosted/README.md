# Hosted Setup

It is hosted on a small VPS on https://crunchbits.com running Ubuntu 22 LTS.  
VPS has 1 CPU Core, 1.5 GB RAM, 40 GB SSD Storage, and an IPv4 address.

I tried my best to get things in the right order and have sudo in the right place, but I probably screwed up some of these directions because I was going fast.

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

At the time of writing this installs NodeJS v20.13.1 and NPM v10.5.2

```shell
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc
nvm install 20
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

The Discord servers need to know what slash commands they should use for the bot.

```shell
NODE_ENV=development node --loader ts-node/esm src/tools/app-command/put-application-commands.ts
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

I thought about this for a while, but eventually decided to stop thinking and take advantage of Grafana Cloud. Grafana Alloy is the current supported and suggested client so I installed it. Still not sure how to see all the things that I need but I followed the documentes to set things up and it seems to work pretty well.

## Development Version of the Bot

As you may have figured out from the `.env` files above but there are dev and prod versions of the bot registered with Discord. The dev version of the bot shouldn't be useful to anybody other than me. It is only online for short bursts so I can test things. None the less I want to have it documented for myself.

https://discord.com/oauth2/authorize?client_id=851611029687500850&permissions=280576&scope=bot%20applications.commands
