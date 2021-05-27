# Shortcuts

Follow local application logs:

```shell
tail -f /opt/app/discord.podcasts/log/application.log
```

Follow system service logs:

```shell
sudo journalctl -u discordpodcasts.service -f
```

Start system service:

```shell
sudo systemctl start discordpodcasts
```

Check system service status:

```shell
sudo systemctl status discordpodcasts
```

Stop system service

```shell
sudo systemctl stop discordpodcasts
```
