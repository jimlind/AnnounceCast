# discord.podcasts

Discord Podcasts Announcement Bot

---

## Commands

#### ?podcasts

> Display the help message.

#### ?podcasts prefix <value>

> Set the bot's custom prefix with the string <value> argument.
> If you don't use this the server will default to `!` as your prefix.

#### <prefix>find <search terms>

> Displays up to 4 podcasts matching the <search terms>.

#### <prefix>following

> Display the podcasts (ids and names) followed in this channel.

#### <prefix>follow <url|search terms>

> Follow a podcast in this channel with the feed URL <url> argument or follow the first podcast matching the search terms

#### <prefix>unfollow <id>

> Unfollow a podcast with the podcast id <id> argument.

#### <prefix>play <id>

> Play the most recent episode of a podcast with the podcast id <id> argument.
> This command requires the issuer to be in a voice channel that the bot has permission to join and speak in.

---

## Use the Hosted Version of the Bot

### Authorize the Bot for Your Server

https://discord.com/oauth2/authorize?client_id=839657120689684533&permissions=278528&scope=bot

### Some Great Podcast URLs to Experiment With

```
70mm             |  https://anchor.fm/s/12d1fabc/podcast/rss
Bat & Spider     |  https://anchor.fm/s/184b0a38/podcast/rss
Cinenauts        |  https://anchor.fm/s/3a0acd20/podcast/rss
Dune Pod         |  https://anchor.fm/s/238d77c8/podcast/rss
Film Hags        |  https://feeds.simplecast.com/DPfrjtYE
Lost Light       |  https://anchor.fm/s/3ae14da0/podcast/rss
Will Run For...  |  https://anchor.fm/s/23694498/podcast/rss
```

---

## Host Your Own Version of the Bot

### Setup

Create a .env file at your project root with your bot token

```
DISCORD_BOT_TOKEN_PROD=ABC.123.XZY.098
```

### System Software Installs

```shell
apt-get install sqlite3
apt-get install python3 --no-install-recommends
apt-get install ffmpeg --no-install-recommends
apt-get install build-essential --no-install-recommends
```

### Service Software Installs

```shell
sudo cp ./docs/discordpodcasts.service /lib/systemd/system/discordpodcasts.service
sudo systemctl daemon-reload
sudo systemctl start discordpodcasts
```
