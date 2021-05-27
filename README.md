# discord.podcasts

Discord Podcasts Announcement Bot

## Install in Discord

https://discord.com/oauth2/authorize?client_id=839657120689684533&scope=bot

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

## Local Installation Guidelines

### System Software Installs

```shell
sudo apt-get install sqlite3
```

### Service Software Installs

```shell
sudo cp ./docs/discordpodcasts.service /lib/systemd/system/discordpodcasts.service
sudo systemctl daemon-reload
sudo systemctl start discordpodcasts
```
