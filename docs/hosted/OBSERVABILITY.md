# Grafana

## Default Setup

I thought about this for a while, but eventually decided to stop thinking and take advantage of free tier of Grafana
Cloud. That limits the length of time and quantity of logs they'll keep (and it takes a while to activate the dashboard)
but for this project I'm well inside the limits of what they offer.

I've authed this projects Grafana against my personal Gmail account. Because I'm running this on a VPS everything here
works as if running on bare metal.

Install Grafana Alloy: https://grafana.com/docs/alloy/latest/set-up/install/linux/
How Grafana wants you to run Alloy: https://grafana.com/docs/alloy/latest/get-started/run/linux/
How Grafana wants you to access Alloy configs: https://grafana.com/docs/alloy/latest/configure/linux/

There is a default Linux Integration that gives a good of the box dashboard to
start: https://grafana.com/docs/grafana-cloud/monitor-infrastructure/integrations/integration-reference/integration-linux-node/

I selected "Extended metrics" and "Advanced set-up" to get the most out of the default install. Then copy pasted as
directed and reloaded the dashboard and I'm getting data. I'll tweak things later but this is a good starting point.

## Application Logging

Next step is getting my application logs into Grafana as well so I can track activity and health of the application.

### Application Log Permissions

The important thing is that logs are set to allow all to read all the way up the parent chain. The /opt/ directory
should be that way, but need to ensure all files and folders have the appropriate permissions.

`> sudo chmod a+rx /opt/bot`
`> sudo chmod a+rx /opt/bot/announcecast`
`> sudo chmod a+rx /opt/bot/announcecast/log`
`> sudo chmod 644 /opt/bot/announcecast/log/activity.log`

### Configuration Customization

I'm not really interested in all the system messages and lots so I'll just change the direct scraper from that
collection of files to the specific application log that I have by changing the __path__ value.

```
local.file_match "logs_integrations_integrations_node_exporter_direct_scrape" {
    path_targets = [{
        __address__ = "localhost",
        __path__    = "/var/log/{syslog,messages,*.log}",
        instance    = "racknerd-456d9e2",
        job         = "integrations/node_exporter",
    }]
}
```

to

```
local.file_match "logs_integrations_integrations_node_exporter_direct_scrape" {
    path_targets = [{
        __address__ = "localhost",
        __path__    = "/opt/bot/announcecast/log/activity.log",
        instance    = constants.hostname,
        job         = "integrations/node_exporter",
    }]
}
```

### Trying to Figure Out "Matches" in loki.souce.journal

Documentation is here: https://grafana.com/docs/alloy/latest/reference/components/loki.source.journal/
PR for code change (and tests) is here: https://github.com/grafana/agent/pull/2825/files

From looking at the code and the test I sorted out that the intent is that the the format is FIELD=value but I couldn't
really tell what it wanted in either of those fields. Through a bunch of trial and error I was able to sort out that it
wants something like the string below. It has been commited to my reference config.alloy file.

```
matches = "_SYSTEMD_UNIT=announcecast.service"
```

When I get my real blog running I need to create an article about this because it wasn't really documented anywhere.

### Allow Shortcuts

Service Interaction

```shell
> systemctl start alloy
> systemctl status alloy
> systemctl restart alloy
> systemctl stop alloy
```

Read the Logs

```shell
> journalctl -e -u alloy
```
