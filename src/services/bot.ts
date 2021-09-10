import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Logger } from 'log4js';
import { IncomingMessage } from '../models/incoming-message';
import { Podcast } from '../models/podcast.js';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage';
import { AudioPlayPodcast } from './audio/audio-play-podcast';
import { AudioVoiceChannel } from './audio/audio-voice-channel';
import { DiscordDataStorage } from './discord/discord-data-storage';
import { DiscordMessageSender } from './discord/discord-message-sender';
import { OutgoingMessageFactory } from './outgoing-message/outgoing-message-factory';
import { PodcastAppleAPIProcessor } from './podcast/podcast-apple-api-processor';
import { PodcastRssProcessor } from './podcast/podcast-rss-processor';

export class Bot {
    static [RESOLVER] = {};

    audioPlayPodcast: AudioPlayPodcast;
    audioVoiceChannel: AudioVoiceChannel;
    discordMessageSender: DiscordMessageSender;
    discordDataStorage: DiscordDataStorage;
    outgoingMessageFactory: OutgoingMessageFactory;
    podcastAppleApiProcessor: PodcastAppleAPIProcessor;
    podcastDataStorage: PodcastDataStorage;
    podcastRssProcessor: PodcastRssProcessor;
    logger: Logger;

    constructor(
        audioPlayPodcast: AudioPlayPodcast,
        audioVoiceChannel: AudioVoiceChannel,
        discordMessageSender: DiscordMessageSender,
        discordDataStorage: DiscordDataStorage,
        outgoingMessageFactory: OutgoingMessageFactory,
        podcastAppleApiProcessor: PodcastAppleAPIProcessor,
        podcastDataStorage: PodcastDataStorage,
        podcastRssProcessor: PodcastRssProcessor,
        logger: Logger,
    ) {
        this.audioPlayPodcast = audioPlayPodcast;
        this.audioVoiceChannel = audioVoiceChannel;
        this.discordMessageSender = discordMessageSender;
        this.discordDataStorage = discordDataStorage;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.podcastAppleApiProcessor = podcastAppleApiProcessor;
        this.podcastDataStorage = podcastDataStorage;
        this.podcastRssProcessor = podcastRssProcessor;
        this.logger = logger;
    }

    actOnUserMessage(incomingMessage: IncomingMessage) {
        switch (incomingMessage.command) {
            case 'find':
                this.find(incomingMessage);
                break;
            case 'following':
                this.following(incomingMessage);
                break;
            case 'follow':
                if (incomingMessage.fromServerManager) {
                    this.follow(incomingMessage);
                } else {
                    this._sendInadequatePermissionsMessage(incomingMessage);
                }
                break;
            case 'unfollow':
                if (incomingMessage.fromServerManager) {
                    this.unfollow(incomingMessage);
                } else {
                    this._sendInadequatePermissionsMessage(incomingMessage);
                }
                break;
            case 'play':
                this.play(incomingMessage);
                break;
            default:
                this.system(incomingMessage);
                break;
        }
    }

    find(incomingMessage: IncomingMessage) {
        const searchTerm = encodeURIComponent(incomingMessage.arguments.join(' '));
        this.podcastAppleApiProcessor
            .search(searchTerm, 4)
            .then((podcastList) => {
                podcastList.forEach((podcast) => {
                    const podcastMessage =
                        this.outgoingMessageFactory.buildPodcastInfoMessage(podcast);
                    this._sendMessageToChannel(incomingMessage.channelId, podcastMessage);
                });
            })
            .catch(() => {
                this.logger.info(`Unable to search on term ${searchTerm}`);
                this._sendErrorToChannel(incomingMessage.channelId);
            });
    }

    follow(incomingMessage: IncomingMessage) {
        const channelId = incomingMessage.channelId;

        this._parseFollowArgumentsToPodcasts(incomingMessage.arguments)
            .then((podcasts: Podcast[]) => {
                podcasts.forEach((podcast) => {
                    this.podcastDataStorage.addFeed(podcast, channelId);
                    const followedMessage = this.outgoingMessageFactory.buildFollowedMessage(
                        podcast,
                        this.podcastDataStorage.getFeedsByChannelId(channelId),
                    );
                    this._sendMessageToChannel(channelId, followedMessage);
                });
            })
            .catch(() => {
                const message = `Error following Podcasts: ${incomingMessage.arguments.join(' ')}`;
                this.logger.info(message);
                this._sendErrorToChannel(channelId);
            });
    }

    unfollow(incomingMessage: IncomingMessage) {
        const channelId = incomingMessage.channelId;
        incomingMessage.arguments.forEach((feedId: string) => {
            const feed = this.podcastDataStorage.getFeedByFeedId(feedId);
            if (!feed) {
                const message = "Use the ID in the 'following' command menu to unfollow a podcast.";
                this.discordMessageSender.sendString(channelId, message);
                return;
            }

            this.podcastDataStorage.removeFeed(feedId, channelId);
            const unfollowedMessage = this.outgoingMessageFactory.buildUnfollowedMessage(
                feed.title,
                this.podcastDataStorage.getFeedsByChannelId(channelId),
            );
            this._sendMessageToChannel(channelId, unfollowedMessage);
        });
    }

    following(incomingMessage: IncomingMessage) {
        const feedList = this.podcastDataStorage.getFeedsByChannelId(incomingMessage.channelId);
        const outgoingMessage = this.outgoingMessageFactory.buildFollowingMessage(feedList);
        this._sendMessageToChannel(incomingMessage.channelId, outgoingMessage);
    }

    play(incomingMessage: IncomingMessage) {
        this.audioVoiceChannel
            .join(incomingMessage)
            .then((voiceConnection) => {
                const feedId = incomingMessage.arguments[0];
                const feedUrl = this.podcastDataStorage.getFeedByFeedId(feedId)?.url || '';

                this.podcastRssProcessor.process(feedUrl, 1).then((podcast) => {
                    this.audioPlayPodcast.play(podcast, voiceConnection, incomingMessage.channelId);
                });
            })
            .catch((message) => {
                this.discordMessageSender.sendString(incomingMessage.channelId, message);
            });
    }

    system(message: IncomingMessage) {
        switch (message.arguments[0]) {
            case 'prefix':
                if (message.fromServerManager) {
                    this.discordDataStorage.setPrefix(message.guildId, message.arguments[1] || '!');
                } else {
                    this._sendInadequatePermissionsMessage(message);
                }
            default:
                const helpMessage = this.outgoingMessageFactory.buildHelpMessage(message.guildId);
                this.discordMessageSender.send(message.channelId, helpMessage);
                break;
        }
    }

    sendNewEpisodeAnnouncement(podcast: Podcast): Promise<void> {
        const outgoingMessage = this.outgoingMessageFactory.buildNewEpisodeMessage(podcast);
        const channelList = this.podcastDataStorage.getChannelsByFeedUrl(podcast.feed);

        const promiseList = channelList
            .map((channelId) => this._sendMessageToChannel(channelId, outgoingMessage))
            .map((p) => p.catch(() => null));

        return Promise.all(promiseList).then(() => {
            this.podcastDataStorage.updatePostedData(podcast.feed, podcast.getFirstEpisode().guid);
        });
    }

    _parseFollowArgumentsToPodcasts(followArguments: string[]): Promise<Podcast[]> {
        // If the arguments start with an http process as feeds otherwise process as show titles
        if (followArguments[0].startsWith('http')) {
            return new Promise((resolve, reject) => {
                const promises = followArguments.map((followArgument: string) => {
                    // Add the extra catch so Promises.all will try everything
                    return this.podcastRssProcessor.process(followArgument, 0).catch(() => false);
                });
                const isPromise = (item: any): item is Promise<Podcast> => item instanceof Promise;
                Promise.all(promises.filter(isPromise)).then((data) => {
                    const podcastList = data.filter(Boolean);
                    if (podcastList.length) {
                        return resolve(podcastList);
                    } else {
                        return reject();
                    }
                });
            });
        } else {
            return this.podcastAppleApiProcessor.search(followArguments.join(' '), 1);
        }
    }

    _sendInadequatePermissionsMessage(incomingMessage: IncomingMessage) {
        const string = 'Only users with Manage Server permissions can perform that action.';
        this.discordMessageSender.sendString(incomingMessage.channelId, string);
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

    _sendErrorToChannel(channelId: string) {
        const message =
            "Something went wrong. Check the bot's permissions, your inputs, and look for curropted podcast feeds.";
        this.discordMessageSender.sendString(channelId, message);
    }
}
