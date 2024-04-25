# AnnounceCast

A simple Discord Podcasts announcement bot.  
Follows some Podcasts on your server and lets you know when new episodes drop.

![Run NPM Tests](https://github.com/jimlind/AnnounceCast/actions/workflows/run-npm-tests.yml/badge.svg)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue?logo=gnu&logoColor=white)](https://www.gnu.org/licenses/agpl-3.0)
[![Chat: Discord](https://img.shields.io/badge/Chat-Discord-7289da?logo=discord&logoColor=white)](https://discord.gg/sEjJTTjG3M)
[![Docs: Available](https://img.shields.io/badge/Docs-Available-green?logo=googledocs&logoColor=white)](https://jimlind.github.io/AnnounceCast/)

---

## Commands

#### /help

> View the help message.

#### /find <keywords>

> Replies with up to 4 podcasts matching the search keyword(s)

#### /following

> Replies with the list of all podcasts (Ids & Names) followed in this channel

#### /follow <keywords> 🔒

> Follow a podcast in this channel matching the search keyword(s)

#### /follow-rss <feed> 🔒

> Follow a podcast in this channel using an RSS feed

#### /unfollow <id> 🔒

> Unfollow a podcast in this channel using the Podcast Id

#### /play <id>

> Play the most recent episode of a podcast using the Podcast Id
> This command requires the issuer to be in a voice channel that the bot has permission to join and speak in.

The 🔒 commands are only available to users with Manage Server permissions.

## Use the Hosted Version of the Bot

### Authorize the Bot for Your Server

PRODUCTION
https://discord.com/oauth2/authorize?client_id=839657120689684533&permissions=280576&scope=bot%20applications.commands

### Some Great Podcasts to Experiment With

```
70mm             |  https://anchor.fm/s/12d1fabc/podcast/rss
Bat & Spider     |  https://anchor.fm/s/184b0a38/podcast/rss
Cinenauts        |  https://anchor.fm/s/3a0acd20/podcast/rss
Dune Pod         |  https://anchor.fm/s/238d77c8/podcast/rss
Film Hags        |  https://feeds.simplecast.com/DPfrjtYE
Lost Light       |  https://anchor.fm/s/3ae14da0/podcast/rss
Will Run For...  |  https://anchor.fm/s/23694498/podcast/rss
```

### Color Codes

AnnounceCast Green is #7ab87a if you want to match a role color for the bot.

---

## Host Your Own Version of the Bot

### Setup

Create a .env file at your project root with some basic bot information.

```
DISCORD_BOT_TOKEN_PROD=ABC.123.XZY.098
DISCORD_CLIENT_ID_PROD=12345678
```

### Node Dependency

Built and tested in Node.js 16.
Other versions will likely work.

### Additional Software Dependencies in Debian Buster

```shell
apt-get install autoconf automake build-essential libtool python3 --no-install-recommends
apt-get install ffmpeg --no-install-recommends
apt-get install sqlite3 --no-install-recommends
```

### Install and run as systemd daemon

```shell
sudo cp ./docs/discordpodcasts.service /lib/systemd/system/discordpodcasts.service
sudo systemctl daemon-reload
sudo systemctl start discordpodcasts
```
