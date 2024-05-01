import { EmbedBuilder, TextChannel } from 'discord.js';
import DiscordConnection from './discord-connection.js';

export default class DiscordMessageSender {
    constructor(readonly discordConnection: DiscordConnection) {}

    public async send(channelId: string, embedBuilder: EmbedBuilder): Promise<boolean> {
        const discordClient = await this.discordConnection.getClient();
        const channel = discordClient.channels.cache.find((ch) => ch.id === channelId);

        if (!(channel instanceof TextChannel)) {
            return false;
        }
        const botPermissions = channel.permissionsFor(discordClient.user || '');
        if (!botPermissions?.has(['ViewChannel', 'SendMessages', 'EmbedLinks'])) {
            return false;
        }
        try {
            await channel.send({ embeds: [embedBuilder] });
        } catch (error) {
            return false;
        }

        return true;
    }
}
