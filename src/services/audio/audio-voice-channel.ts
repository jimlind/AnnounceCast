import {
    getVoiceConnection,
    joinVoiceChannel,
    VoiceConnection,
    VoiceConnectionStatus,
} from '@discordjs/voice';
import { RESOLVER } from 'awilix';
import { StageChannel, VoiceChannel } from 'discord.js';
import { DiscordConnection } from '../discord/discord-connection.js';

export class AudioVoiceChannel {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    discordConnection: DiscordConnection;
    constructor(discordConnection: DiscordConnection) {
        this.discordConnection = discordConnection;
    }

    join(channel: VoiceChannel | StageChannel): Promise<VoiceConnection> {
        return new Promise((resolve, reject) => {
            // Make sure the bot has permissions to the voice channel
            const user = this.discordConnection.getClient().user || '';
            const permissions = channel.permissionsFor(user);
            if (!permissions?.has(['VIEW_CHANNEL', 'SPEAK'])) {
                return reject();
            }

            const voiceConnection = joinVoiceChannel({
                channelId: channel.id,
                guildId: channel.guild.id,
                adapterCreator: channel.guild.voiceAdapterCreator,
            });
            voiceConnection.on(VoiceConnectionStatus.Ready, () => {
                return resolve(voiceConnection);
            });
        });
    }

    leave(channel: VoiceChannel | StageChannel | null) {
        const voiceConnection = getVoiceConnection(channel?.guild.id || '');
        voiceConnection?.destroy();
    }

    createDiscordJSAdapter() {}
}
