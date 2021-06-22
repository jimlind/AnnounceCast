import { RESOLVER } from 'awilix';
import { VoiceConnection } from 'discord.js';
import { IncomingMessage } from '../../models/incoming-message.js';
import { DiscordConnection } from '../discord/discord-connection.js';

export class AudioVoiceChannel {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    discordConnection: DiscordConnection;
    constructor(discordConnection: DiscordConnection) {
        this.discordConnection = discordConnection;
    }

    join(incomingMessage: IncomingMessage): Promise<VoiceConnection> {
        return new Promise((resolve, reject) => {
            // Make sure user is in a voice channel
            if (!incomingMessage.voiceChannel) {
                return reject('You must also be in a voice channel to play a podcast.');
            }

            // Make sure the bot has permissions to the voice channel
            const botUser = this.discordConnection.getClient().user || '';
            const botPermissions = incomingMessage.voiceChannel.permissionsFor(botUser);
            if (!botPermissions?.has(['VIEW_CHANNEL', 'SPEAK'])) {
                return reject('Bot must have permissions in a voice channel to play a podcast.');
            }

            // TODO: Can this Voice Channel be on a different server?
            // If the user is on a voice channel in one guild can the bot override the voice channel of another

            // Attempt to join the voice channel
            incomingMessage.voiceChannel
                .join()
                .then((voiceConnection) => {
                    return resolve(voiceConnection);
                })
                .catch(() => {
                    return reject('Bot unable to join voice channel.');
                });
        });
    }
}
