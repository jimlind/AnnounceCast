import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Logger } from 'log4js';
import { IncomingMessage } from '../models/incoming-message';
import { PodcastEpisode } from '../models/podcast-episode';
import { Podcast } from '../models/podcast.js';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage';
import { DiscordDataStorage } from './discord/discord-data-storage';
import { DiscordMessageSender } from './discord/discord-message-sender';
import { OutgoingMessageFactory } from './outgoing-message/outgoing-message-factory';
import { PodcastAppleAPIProcessor } from './podcast/podcast-apple-api-processor';
import { PodcastRssProcessor } from './podcast/podcast-rss-processor';

export class Bot {
    static [RESOLVER] = {};

    discordMessageSender: DiscordMessageSender;
    discordDataStorage: DiscordDataStorage;
    outgoingMessageFactory: OutgoingMessageFactory;
    podcastAppleApiProcessor: PodcastAppleAPIProcessor;
    podcastDataStorage: PodcastDataStorage;
    podcastRssProcessor: PodcastRssProcessor;
    logger: Logger;

    constructor(
        discordMessageSender: DiscordMessageSender,
        discordDataStorage: DiscordDataStorage,
        outgoingMessageFactory: OutgoingMessageFactory,
        podcastAppleApiProcessor: PodcastAppleAPIProcessor,
        podcastDataStorage: PodcastDataStorage,
        podcastRssProcessor: PodcastRssProcessor,
        logger: Logger,
    ) {
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
        // TODO: If it isn't a URL use the podcastAppleApiProcessor
        incomingMessage.arguments.forEach((feedUrl: string) => {
            this.podcastRssProcessor
                .process(feedUrl, 0)
                .then((podcast: Podcast) => {
                    this.podcastDataStorage.addFeed(podcast, channelId);
                    const followedMessage = this.outgoingMessageFactory.buildFollowedMessage(
                        podcast,
                        this.podcastDataStorage.getFeedsByChannelId(channelId),
                    );
                    this._sendMessageToChannel(channelId, followedMessage);
                })
                .catch(() => {
                    this.logger.info(`Unable to Follow Podcast ${feedUrl}`);
                    this._sendErrorToChannel(channelId);
                });
        });
    }

    unfollow(incomingMessage: IncomingMessage) {
        incomingMessage.arguments.forEach((feedId: string) => {
            const feed = this.podcastDataStorage.getFeedByFeedId(feedId);
            this.podcastDataStorage.removeFeed(feedId, incomingMessage.channelId);
            const unfollowedMessage = this.outgoingMessageFactory.buildUnfollowedMessage(
                feed.title,
                this.podcastDataStorage.getFeedsByChannelId(incomingMessage.channelId),
            );
            this._sendMessageToChannel(incomingMessage.channelId, unfollowedMessage);
        });
    }

    following(incomingMessage: IncomingMessage) {
        const feedList = this.podcastDataStorage.getFeedsByChannelId(incomingMessage.channelId);
        const outgoingMessage = this.outgoingMessageFactory.buildFollowingMessage(feedList);
        this._sendMessageToChannel(incomingMessage.channelId, outgoingMessage);
    }

    // TODO: This is a mess.
    play(message: IncomingMessage) {
        if (!message.voiceChannel) {
            this.discordMessageSender.sendString(
                message.channelId,
                'You must also be in a voice channel to play a podcast.',
            );
            return;
        }

        // const user = discordMessage.client.user || '';
        // if (voiceChannel && voiceChannel.permissionsFor(user)?.has('SPEAK')) {

        // TODO: Can this Voice Channel be on a different server?
        const voiceChannel = message.voiceChannel;
        const voiceChannelName = voiceChannel.name;

        const feedUrl = this.podcastDataStorage.getFeedByFeedId(message.arguments[0]).url;
        this.podcastRssProcessor
            .process(feedUrl, 0)
            .then((podcast: any) => {
                voiceChannel
                    .join()
                    .then((connection) => {
                        this.logger.debug(`[play] Attempting to play ${podcast.showTitle}`);
                        connection
                            .play(podcast.showAudio)
                            .on('start', () => {
                                this.logger.debug(`[play] Started playing ${podcast.showTitle}`);
                                this.discordMessageSender.sendString(
                                    message.channelId,
                                    `Podcast has started playing in ${voiceChannelName}`,
                                );
                            })
                            .on('finish', () => {
                                this.logger.debug(`[play] Completed playing ${podcast.showTitle}`);
                                voiceChannel.leave();
                            })
                            .on('error', (error) => {
                                this.logger.error(`[play] Unable to Play Podcast [${error}]`);
                                this.discordMessageSender.sendString(
                                    message.channelId,
                                    `Error trying to play the podcast in ${voiceChannelName}`,
                                );
                            });
                    })
                    .catch((e) => {
                        console.log(e);
                        this.logger.error(`[play] Unable to Join Voice Channel`);
                        this.discordMessageSender.sendString(
                            message.channelId,
                            `Bot was unable to join ${voiceChannelName} to play podcast`,
                        );
                    });
            })
            .catch(() => {
                this.logger.error(`[play] Unable to Process Podcast`);
                this.discordMessageSender.sendString(
                    message.channelId,
                    `There was a problem with the selected podcast feed.`,
                );
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

    sendNewEpisodeAnnouncement(podcast: Podcast) {
        const outgoingMessage = this.outgoingMessageFactory.buildNewEpisodeMessage(podcast);

        const channelList = this.podcastDataStorage.getChannelsByFeedUrl(podcast.feed);
        channelList.forEach((channelId: string) => {
            this._sendMessageToChannel(channelId, outgoingMessage).then(() => {
                this.podcastDataStorage.updatePostedData(
                    podcast.feed,
                    podcast.getFirstEpisode().guid,
                );
            });
        });
    }

    // TODO: Maybe this should live somewhere else.
    podcastHasLatestEpisode(podcast: Podcast): boolean {
        const guid = this.podcastDataStorage.getPostedFromUrl(podcast.feed);
        return podcast.episodeList.reduce((accumulator: boolean, current: PodcastEpisode) => {
            return accumulator || current.guid == guid;
        }, false);
    }

    _sendInadequatePermissionsMessage(incomingMessage: IncomingMessage) {
        const string = 'Only users with Manage Server permissions can perform that action.';
        this.discordMessageSender.sendString(incomingMessage.channelId, string);
    }

    _sendMessageToChannel(channelId: string, outgoingMessage: MessageEmbed): Promise<void> {
        return new Promise((resolve) => {
            this.discordMessageSender
                .send(channelId, outgoingMessage)
                .then(() => {
                    const data = { title: outgoingMessage.title, channelId };
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
            'Something went wrong. Check the bot has all the proper permissions and that the podcasts you are following are not corrupted.';
        this.discordMessageSender.sendString(channelId, message);
    }
}
