import { RESOLVER } from 'awilix';
import {
    CommandInteraction,
    GuildMember,
    MessageEmbed,
    StageChannel,
    VoiceChannel,
} from 'discord.js';
import { Logger } from 'log4js';
import { Podcast } from '../models/podcast.js';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage';
import { AudioPlayPodcast } from './audio/audio-play-podcast';
import { AudioVoiceChannel } from './audio/audio-voice-channel';
import { DiscordMessageSender } from './discord/discord-message-sender';
import { OutgoingMessageFactory } from './outgoing-message/outgoing-message-factory';
import { PodcastAppleAPIProcessor } from './podcast/podcast-apple-api-processor';
import { PodcastRssProcessor } from './podcast/podcast-rss-processor';

export class Bot {
    static [RESOLVER] = {};

    audioPlayPodcast: AudioPlayPodcast;
    audioVoiceChannel: AudioVoiceChannel;
    discordMessageSender: DiscordMessageSender;
    outgoingMessageFactory: OutgoingMessageFactory;
    podcastAppleApiProcessor: PodcastAppleAPIProcessor;
    podcastDataStorage: PodcastDataStorage;
    podcastRssProcessor: PodcastRssProcessor;
    logger: Logger;

    constructor(
        audioPlayPodcast: AudioPlayPodcast,
        audioVoiceChannel: AudioVoiceChannel,
        discordMessageSender: DiscordMessageSender,
        outgoingMessageFactory: OutgoingMessageFactory,
        podcastAppleApiProcessor: PodcastAppleAPIProcessor,
        podcastDataStorage: PodcastDataStorage,
        podcastRssProcessor: PodcastRssProcessor,
        logger: Logger,
    ) {
        this.audioPlayPodcast = audioPlayPodcast;
        this.audioVoiceChannel = audioVoiceChannel;
        this.discordMessageSender = discordMessageSender;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.podcastAppleApiProcessor = podcastAppleApiProcessor;
        this.podcastDataStorage = podcastDataStorage;
        this.podcastRssProcessor = podcastRssProcessor;
        this.logger = logger;
    }

    actOnCommandInteraction(commandInteraction: CommandInteraction) {
        if (this._isRestrictedCommand(commandInteraction)) {
            return this._sendInadequatePermissionsMessage(commandInteraction);
        }

        switch (commandInteraction.commandName) {
            case 'find':
                this.find(commandInteraction);
                break;
            case 'following':
                this.following(commandInteraction);
                break;
            case 'follow':
                this.follow(commandInteraction);
                break;
            case 'follow-rss':
                this.followRss(commandInteraction);
                break;
            case 'unfollow':
                this.unfollow(commandInteraction);
                break;
            case 'play':
                this.play(commandInteraction);
                break;
            default:
                this.help(commandInteraction);
                break;
        }
    }

    find(commandInteraction: CommandInteraction) {
        const searchKeywords = commandInteraction.options.getString('keywords') || '';
        this.podcastAppleApiProcessor
            .search(searchKeywords, 4)
            .then((podcastList) => {
                // Generic response for empty results
                if (podcastList.length == 0) {
                    return commandInteraction.editReply('No podcasts found.');
                }
                // Create list of embed messages to push out
                const embedList = podcastList.map((podcast) =>
                    this.outgoingMessageFactory.buildPodcastInfoMessage(podcast),
                );
                return commandInteraction.editReply({ embeds: embedList });
            })
            .catch(() => {
                this.logger.info(`Unable to search on term ${searchKeywords}`);
                return this._sendErrorToChannel(commandInteraction);
            });
    }

    follow(commandInteraction: CommandInteraction) {
        const searchKeywords = commandInteraction.options.getString('keywords') || '';
        this.podcastAppleApiProcessor
            .search(searchKeywords, 1)
            .then((podcastList) => {
                this._followPodcastList(commandInteraction, podcastList);
            })
            .catch(() => {
                this.logger.info(`Unable to follow on term ${searchKeywords}`);
                return this._sendErrorToChannel(commandInteraction);
            });
    }

    followRss(commandInteraction: CommandInteraction) {
        const feedUrl = commandInteraction.options.getString('feed') || '';
        this.podcastRssProcessor
            .process(feedUrl, 0)
            .then((podcast) => {
                this._followPodcastList(commandInteraction, [podcast]);
            })
            .catch(() => {
                this.logger.info(`Unable to follow on feed ${feedUrl}`);
                return this._sendErrorToChannel(commandInteraction);
            });
    }

    unfollow(commandInteraction: CommandInteraction) {
        const feedId = commandInteraction.options.getString('id') || '';
        const feed = this.podcastDataStorage.getFeedByFeedId(feedId);
        if (!feed) {
            return this._sendErrorToChannel(commandInteraction);
        }

        const channelId = commandInteraction.channelId;
        this.podcastDataStorage.removeFeed(feedId, channelId);
        const message = this.outgoingMessageFactory.buildUnfollowedMessage(
            feed.title,
            this.podcastDataStorage.getFeedsByChannelId(channelId),
        );
        commandInteraction.editReply({ embeds: [message] });
    }

    following(commandInteraction: CommandInteraction) {
        const feedList = this.podcastDataStorage.getFeedsByChannelId(commandInteraction.channelId);
        const outgoingMessage = this.outgoingMessageFactory.buildFollowingMessage(feedList);
        commandInteraction.editReply({ embeds: [outgoingMessage] });
    }

    play(commandInteraction: CommandInteraction) {
        const getPodcast = () => {
            const feedId = commandInteraction.options.getString('id');
            const feedUrl = this.podcastDataStorage.getFeedByFeedId(feedId || '')?.url || '';
            return this.podcastRssProcessor.process(feedUrl, 1);
        };

        const getVoiceConnection = (channel: VoiceChannel | StageChannel) => {
            return this.audioVoiceChannel.join(channel);
        };

        const member = commandInteraction.member;
        if (!(member instanceof GuildMember && member?.voice.channel)) {
            return commandInteraction.editReply('You must be in voice chat to start audio playing');
        }

        return Promise.all([getPodcast(), getVoiceConnection(member?.voice.channel)])
            .then(([podcast, voiceConnection]) => {
                const episode = podcast.getFirstEpisode();
                const message = `Playing ${episode.title} from ${podcast.title}`;
                this.audioPlayPodcast.play(episode, voiceConnection);
                return commandInteraction.editReply(message);
            })
            .catch(() => {
                this.audioVoiceChannel.leave(member?.voice.channel);
                return this._sendErrorToChannel(commandInteraction);
            });
    }

    help(commandInteraction: CommandInteraction) {
        const message = this.outgoingMessageFactory.buildHelpMessage();
        return commandInteraction.editReply({ embeds: [message] });
    }

    sendNewEpisodeAnnouncement(podcast: Podcast): Promise<void> {
        const outgoingMessage = this.outgoingMessageFactory.buildNewEpisodeMessage(podcast);
        const channelList = this.podcastDataStorage.getChannelsByFeedUrl(podcast.feed);

        const promiseList = channelList
            .map((channelId : any) => this._sendMessageToChannel(channelId, outgoingMessage))
            .map((p : any) => p.catch(() => null));

        return Promise.all(promiseList).then(() => {
            this.podcastDataStorage.updatePostedData(podcast.feed, podcast.getFirstEpisode().guid);
        });
    }

    _isRestrictedCommand(commandInteraction: CommandInteraction) {
        // If command is not on the list of protected commands, exit early
        const protectedCommands = ['follow', 'follow-rss', 'unfollow'];
        if (!protectedCommands.includes(commandInteraction.commandName)) {
            return false;
        }

        // If member is not a normal guild member, exit early
        if (!(commandInteraction.member instanceof GuildMember)) {
            return false;
        }

        // Return opposite of manage permissions
        return !commandInteraction.member.permissions.has('MANAGE_GUILD');
    }

    _sendInadequatePermissionsMessage(commandInteraction: CommandInteraction) {
        const message = 'Only users with Manage Server permissions can perform that action.';
        return commandInteraction.editReply(message);
    }

    // This only resolves, it never rejects.
    // If a messages doesn't send in a channel there's a log, but I'm not going to try and notify the channel
    _sendMessageToChannel(channelId: string, outgoingMessage: MessageEmbed): Promise<void> {
        return new Promise((resolve) => {
            this.discordMessageSender
                .send(channelId, outgoingMessage)
                .then(() => {
                    const memory = (process.memoryUsage().heapUsed / 1024 / 1024).toFixed(2) + 'MB';
                    const data = { title: outgoingMessage.title, channelId, memory };
                    this.logger.info(`Message Send Success: ${JSON.stringify(data)}`);
                })
                .catch((error: string) => {
                    const data = {
                        message: outgoingMessage.toJSON(),
                        channelId,
                        error,
                    };
                    this.logger.error(`Message Send Failure: ${JSON.stringify(data)}`);
                })
                .finally(() => {
                    return resolve();
                });
        });
    }

    _sendErrorToChannel(commandInteraction: CommandInteraction) {
        const message =
            "Something went wrong. Check the bot's permissions, your input, and the podcast data.";
        return commandInteraction.editReply(message);
    }

    _followPodcastList(commandInteraction: CommandInteraction, podcastList: Podcast[]) {
        if (podcastList.length !== 1) {
            return this._sendErrorToChannel(commandInteraction);
        }

        this.podcastDataStorage.addFeed(podcastList[0], commandInteraction.channelId);
        const message = this.outgoingMessageFactory.buildFollowedMessage(
            podcastList[0],
            this.podcastDataStorage.getFeedsByChannelId(commandInteraction.channelId),
        );
        commandInteraction.editReply({ embeds: [message] });
    }
}
