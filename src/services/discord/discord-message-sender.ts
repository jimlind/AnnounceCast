import { RESOLVER } from 'awilix';
import { Client, DiscordAPIError, MessageEmbed, TextChannel } from 'discord.js';
import { DiscordConnection } from './discord-connection.js';

export class DiscordMessageSender {
    static [RESOLVER] = {};

    discordConnection: DiscordConnection;

    constructor(discordConnection: DiscordConnection) {
        this.discordConnection = discordConnection;
    }

    send(channelId: string, message: MessageEmbed): Promise<string> {
        return new Promise((resolve: Function, reject: Function) => {
            this.discordConnection.getConnectedClient().then((client: Client) => {
                const channel = client.channels.cache.find((ch) => ch.id === channelId);

                if (!(channel instanceof TextChannel)) {
                    return reject('No Text Channel Found');
                }

                const botPermissions = channel.permissionsFor(client.user || '');
                if (!botPermissions?.has(['VIEW_CHANNEL', 'SEND_MESSAGES', 'EMBED_LINKS'])) {
                    return reject('Channel Has Bad Permissions');
                }

                channel
                    .send(message)
                    .then(() => {
                        return resolve('Successfully Sent Message');
                    })
                    .catch((e) => {
                        if (e instanceof DiscordAPIError) {
                            return reject(e.message);
                        }
                        return reject('Unknown Error');
                    });
            });
        });
    }
}
