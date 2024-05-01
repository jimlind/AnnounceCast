# AnnounceCast

A simple Discord Podcasts announcement bot.  
Follows some Podcasts on your server and lets you know when new episodes drop.

![Run NPM Tests](https://github.com/jimlind/AnnounceCast/actions/workflows/run-npm-tests.yml/badge.svg)
![Run NPM Lint](https://github.com/jimlind/AnnounceCast/actions/workflows/run-npm-lint.yml/badge.svg)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue?logo=gnu&logoColor=white)](https://www.gnu.org/licenses/agpl-3.0)
[![Chat: Discord](https://img.shields.io/badge/Chat-Discord-7289da?logo=discord&logoColor=white)](https://discord.gg/sEjJTTjG3M)
[![Docs: Available](https://img.shields.io/badge/Docs-Available-green?logo=googledocs&logoColor=white)](https://jimlind.github.io/AnnounceCast/)

---

## [Using the Hosted Bot](https://jimlind.github.io/AnnounceCast/)

It is likely that you want to figure out how to use my hosted bot on your Discord server. You can find [that documentationt here](https://jimlind.github.io/AnnounceCast/).

## Hosting Your Own Bot

If you want to host your own version of the bot or use this code as a starting point for building your own bot that's what the rest of the information on this page is about. It is extremely unlikely that this is what you are actually looking for. You probably want to click the link in the paragraph above.

### Setup

Built and tested in Node.js 20 LTS. Other versions will likely work.

#### Create Config Files

Create `.env.development` and `.evn.production` files at your project root with some basic bot information.

```
DISCORD_BOT_TOKEN_PROD=ABC.123.XZY.098
DISCORD_CLIENT_ID_PROD=12345678
```

#### Install Dependencies

```shell
> apt-get install sqlite3 --no-install-recommends
> npm install
```

### Some Great Podcasts to Experiment With

```
70mm             |  https://anchor.fm/s/12d1fabc/podcast/rss
Bat & Spider     |  https://anchor.fm/s/184b0a38/podcast/rss
Dune Pod         |  https://anchor.fm/s/238d77c8/podcast/rss
Lost Light       |  https://anchor.fm/s/3ae14da0/podcast/rss
Will Run For...  |  https://anchor.fm/s/23694498/podcast/rss
```

### Color Codes

AnnounceCast Green is #7ab87a if you want to match a role color for the bot.

---

### Additional Software Dependencies in Debian Buster

```shell
apt-get install sqlite3 --no-install-recommends
```

### Install and run as systemd daemon

```shell
sudo cp ./docs/discordpodcasts.service /lib/systemd/system/discordpodcasts.service
sudo systemctl daemon-reload
sudo systemctl start discordpodcasts
```
