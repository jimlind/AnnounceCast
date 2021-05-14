import { RESOLVER } from 'awilix';
import { Client, MessageEmbed, TextChannel } from 'discord.js';
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
                    return reject('Unable to Send Message: Bad Channel Id');
                }

                if (!channel.viewable) {
                    return reject('Unable to Send Message: Channel Not Visibile');
                }

                channel
                    .send(message)
                    .then(() => {
                        return resolve('Successfully Sent Message');
                    })
                    .catch(() => {
                        return reject('Unable to Send Message: Bad Channel Permissions');
                    });
            });
        });
    }
}
