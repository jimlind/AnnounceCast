import { CacheType, ChatInputCommandInteraction, EmbedBuilder } from 'discord.js';
import DiscordMessageSender from '../../discord/discord-message-sender.js';
import OutgoingMessageFactory from '../../outgoing-message/outgoing-message-factory.js';

interface HelpCommandInterface {
    readonly discordMessageSender: DiscordMessageSender;
    readonly outgoingMessageFactory: OutgoingMessageFactory;

    execute(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class HelpCommand implements HelpCommandInterface {
    constructor(
        readonly discordMessageSender: DiscordMessageSender,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
    ) {}

    public async execute(interaction: ChatInputCommandInteraction<CacheType>) {
        const executeTest = interaction.options.getBoolean('test') || false;
        if (executeTest) {
            const messageList = this.outgoingMessageFactory.buildHelpTestMessageList();
            await interaction.editReply({ embeds: [<EmbedBuilder>messageList.shift()] });
            for (const message of messageList) {
                await this.discordMessageSender.sendMessageToChannel(
                    interaction.channelId,
                    message,
                );
            }
            return;
        }

        const message = await this.outgoingMessageFactory.buildHelpMessage();
        await interaction.editReply({ embeds: [message] });
    }
}
