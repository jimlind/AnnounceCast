import { CacheType, ChatInputCommandInteraction } from 'discord.js';
import { Logger } from 'log4js';
import DiscordMessageSender from './discord/discord-message-sender.js';
import FollowCommand from './interaction-command/commands/follow-command.js';
import FollowRssCommand from './interaction-command/commands/follow-rss-command.js';
import FollowingCommand from './interaction-command/commands/following-command.js';
import HelpCommand from './interaction-command/commands/help-command.js';
import SearchCommand from './interaction-command/commands/search-command.js';
import UnfollowCommand from './interaction-command/commands/unfollow-command.js';

interface BotInterface {
    readonly discordMessageSender: DiscordMessageSender;
    readonly followCommand: FollowCommand;
    readonly followingCommand: FollowingCommand;
    readonly followRssCommand: FollowRssCommand;
    readonly helpCommand: HelpCommand;
    readonly logger: Logger;
    readonly searchCommand: SearchCommand;
    readonly unfollowCommand: UnfollowCommand;

    receiveInteraction(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class Bot implements BotInterface {
    constructor(
        readonly discordMessageSender: DiscordMessageSender,
        readonly followCommand: FollowCommand,
        readonly followingCommand: FollowingCommand,
        readonly followRssCommand: FollowRssCommand,
        readonly helpCommand: HelpCommand,
        readonly logger: Logger,
        readonly searchCommand: SearchCommand,
        readonly unfollowCommand: UnfollowCommand,
    ) {}

    async receiveInteraction(interaction: ChatInputCommandInteraction<CacheType>) {
        try {
            switch (interaction.commandName) {
                case 'search':
                    await this.searchCommand.execute(interaction);
                    break;
                case 'follow':
                    await this.followCommand.execute(interaction);
                    break;
                case 'follow-rss':
                    await this.followRssCommand.execute(interaction);
                    break;
                case 'unfollow':
                    await this.unfollowCommand.execute(interaction);
                    break;
                case 'following':
                    await this.followingCommand.execute(interaction);
                    break;
                default:
                    this.helpCommand.execute(interaction);
                    break;
            }
        } catch (error) {
            const title = `receiveInteraction method failed for ${interaction.commandName}`;
            this.logger.info(title, { interaction, error });
            await this.discordMessageSender.sendErrorAsReply(interaction);
        }
    }
}
