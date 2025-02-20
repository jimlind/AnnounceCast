# Hosted Setup

This document mostly serves as a record for myself so that I don't have to try to remember everything. Maybe other
people might get some value from it, but that seems extremely doubtful.

It is hosted on a VPS on https://www.racknerd.com running Ubuntu 24 LTS. VPS has 2 CPU Cores, 2.5 GB RAM, 40 GB SSD
Storage, and an IPv4 address. I got it from a Black Friday thread on https://lowendtalk.com/ to replace a host that is
discontinuing the plan I was on. It has a 6TB/month traffic allotment that I assume I'll go over as the bot's reach
increases.

## Why Ubuntu 24?

I wanted a release that natively supported `systemd-creds` without needing to update a bunch of other dependencies and
still have a long shelf life.

## Other References

Everything related to running the bot (setting up the service and such) is covered in the RUNNING.MD file.  
Everything related to deploying the bot (from a GitHub action) is covered in the DEPLOY.MD file.  
Everything related to observability (watching Grafana) is covered in the OBSERVABILITY.MD file.

## Publish Slash Commands

The Discord servers need to know what slash commands they should use for the bot. I set this up for the NodeJS version
of the bot and I haven't written a version for it in Java yet. The old command could probably be revived from the old
branch if necessary

```shell
NODE_ENV=production node --import ./register.mjs ./src/tools/app-command/put-application-commands.ts
```

## Miscellaneous Shortcuts

Service Interaction

```shell
> systemctl start announcecast
> systemctl status announcecast
> systemctl restart announcecast
> systemctl stop announcecast
```

Read the Logs

```shell
> tail -f /opt/bot/announcecast/log/activity.log
> journalctl -e -u announcecast
```
