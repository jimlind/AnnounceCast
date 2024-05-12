import { CacheType, ChatInputCommandInteraction, EmbedBuilder } from 'discord.js';
import { Logger } from 'log4js';
import DiscordMessageSender from '../discord/discord-message-sender.js';

interface CommandHelpersInterface {
    readonly discordMessageSender: DiscordMessageSender;
    readonly logger: Logger;

    sendMessageToChannel(channelId: string, embedBuilder: EmbedBuilder): Promise<void>;
    sendNoMatchesMessageAsReply(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class CommandHelpers implements CommandHelpersInterface {
    constructor(
        readonly discordMessageSender: DiscordMessageSender,
        readonly logger: Logger,
    ) {}

    public async sendMessageToChannel(
        channelId: string,
        embedBuilder: EmbedBuilder,
    ): Promise<void> {
        try {
            await this.discordMessageSender.send(channelId, embedBuilder);

            const memory = (process.memoryUsage().heapUsed / 1024 / 1024).toFixed(2) + 'MB';
            this.logger.info('Message Send Success', {
                title: embedBuilder.data.title,
                channelId,
                memory,
            });
        } catch (error) {
            this.logger.error('Message Send Failure', {
                message: embedBuilder.toJSON(),
                channelId,
                error,
            });
        }
        return;
    }

    public sendNoMatchesMessageAsReply(interaction: ChatInputCommandInteraction<CacheType>) {
        return interaction.editReply('Nothing was found matching your query.');
    }
}
