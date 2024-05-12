import { CacheType, ChatInputCommandInteraction, EmbedBuilder } from 'discord.js';
import OutgoingMessageFactory from '../../outgoing-message/outgoing-message-factory.js';
import CommandHelpers from '../command-helpers.js';

interface HelpCommandInterface {
    readonly commandHelpers: CommandHelpers;
    readonly outgoingMessageFactory: OutgoingMessageFactory;

    execute(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class HelpCommand implements HelpCommandInterface {
    constructor(
        readonly commandHelpers: CommandHelpers,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
    ) {}

    public async execute(interaction: ChatInputCommandInteraction<CacheType>) {
        const executeTest = interaction.options.getBoolean('test') || false;
        if (executeTest) {
            const messageList = this.outgoingMessageFactory.buildHelpTestMessageList();
            await interaction.editReply({ embeds: [<EmbedBuilder>messageList.shift()] });
            for (const message of messageList) {
                await this.commandHelpers.sendMessageToChannel(interaction.channelId, message);
            }
            return;
        }

        const message = await this.outgoingMessageFactory.buildHelpMessage();
        await interaction.editReply({ embeds: [message] });
    }
}
