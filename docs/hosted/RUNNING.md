Create the user that is going to be running the bot
`> useradd -r bot`

Create the location that bot is going to be running in
`> mkdir -p /opt/bot/announcecast`
`> cd /opt/announcecast`
`> wget https://github.com/jimlind/AnnounceCast/releases/latest/download/announcecast.jar`

If you don't have a database to put in place it will get created automatically.
But you'll probably be reusing an existing one.
`> mkdir -p /opt/bot/announcecast/db`
`> scp db/podcasts.db vps:/opt/bot/announecast/db/podcasts.db`

Make sure that the log and db directories (and any existing files) are writable by the bot user

Create the encrypted properties file
Assuming you have a file following the format in our input/properties.tmp file but is named `properties`
Run this command on the VPS so that it can be decrypted on the same machine
`> systemd-creds encrypt --name=properties properties ciphertext.cred`

Copy the systemd files and start the system.
`> scp docs/hosted/announcecast.service vps:/etc/systemd/system/announcecast.service`
`> systemctl daemon-reload && systemctl start announcecast.service` will start it.
`> systemctl status announcecast.service` to check the status of it
`> journalctl -e -u announcecast.service` to view the logs from it (jumping to the end).

Enable starting the service on reboot.
`> systemctl enable announcecast.service`