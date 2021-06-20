import { RESOLVER } from 'awilix';
import { AxiosInstance } from 'axios';
import { MessageEmbed } from 'discord.js';
import { Logger } from 'log4js';
import { IncomingMessage } from '../models/incoming-message';
import { Podcast } from '../models/podcast.js';
import { PodcastEpisode } from '../models/podcast-episode';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage';
import { DiscordDataStorage } from './discord/discord-data-storage';
import { DiscordMessageFactory } from './discord/discord-message-factory';
import { DiscordMessageSender } from './discord/discord-message-sender';
import { OutgoingMessageFactory } from './outgoing-message/outgoing-message-factory';
import { PodcastRssProcessor } from './podcast/podcast-rss-processor';

export class Bot {
    static [RESOLVER] = {};

    axios: AxiosInstance;
    discordMessageFactory: DiscordMessageFactory;
    discordMessageSender: DiscordMessageSender;
    discordDataStorage: DiscordDataStorage;
    outgoingMessageFactory: OutgoingMessageFactory;
    podcastDataStorage: PodcastDataStorage;
    podcastRssProcessor: PodcastRssProcessor;
    logger: Logger;

    constructor(
        axios: AxiosInstance,
        discordMessageFactory: DiscordMessageFactory,
        discordMessageSender: DiscordMessageSender,
        discordDataStorage: DiscordDataStorage,
        outgoingMessageFactory: OutgoingMessageFactory,
        podcastDataStorage: PodcastDataStorage,
        podcastRssProcessor: PodcastRssProcessor,
        logger: Logger,
    ) {
        this.axios = axios;
        this.discordMessageFactory = discordMessageFactory;
        this.discordMessageSender = discordMessageSender;
        this.discordDataStorage = discordDataStorage;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.podcastDataStorage = podcastDataStorage;
        this.podcastRssProcessor = podcastRssProcessor;
        this.logger = logger;
    }

    actOnUserMessage(message: IncomingMessage) {
        switch (message.command) {
            case 'find':
                this.find(message);
                break;
            case 'following':
                this.following(message);
                break;
            case 'follow':
                if (message.fromServerManager) {
                    this.follow(message);
                } else {
                    this.sendInadequatePermissionsMessage(message);
                }
                break;
            case 'unfollow':
                if (message.fromServerManager) {
                    this.unfollow(message);
                } else {
                    this.sendInadequatePermissionsMessage(message);
                }
                break;
            case 'play':
                this.play(message);
                break;
            default:
                this.system(message);
                break;
        }
    }

    // TODO: Something with this
    find(message: IncomingMessage) {
        const searchTerm = encodeURIComponent(message.arguments.join(' '));
        const url = `https://itunes.apple.com/search?term=${searchTerm}&country=US&media=podcast&attribute=titleTerm&limit=4`;
        this.axios.get(url).then((response: any) => {
            const data = response.data;
            data.results.forEach((result: any) => {
                console.log(result.collectionName);
                console.log(result.artistName);
                console.log(result.collectionViewUrl);
                console.log(result.feedUrl);
                console.log(result.artworkUrl600);
            });
        });
    }

    follow(incomingMessage: IncomingMessage) {
        const channelId = incomingMessage.channelId;
        // TODO: If it isn't a URL use the find
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
            const response = this.discordMessageFactory.buildMessage();
            response.setDescription('You must also be in a voice channel to play a podcast.');
            this.discordMessageSender.send(message.channelId, response);
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
                                const response = this.discordMessageFactory.buildMessage();
                                response.setDescription(
                                    `Podcast has started playing in ${voiceChannelName}`,
                                );
                                this.discordMessageSender.send(message.channelId, response);
                            })
                            .on('finish', () => {
                                this.logger.debug(`[play] Completed playing ${podcast.showTitle}`);
                                voiceChannel.leave();
                            })
                            .on('error', (error) => {
                                this.logger.error(`[play] Unable to Play Podcast [${error}]`);
                                const response = this.discordMessageFactory.buildMessage();
                                response.setDescription(
                                    `Error trying to play the podcast in ${voiceChannelName}`,
                                );
                                this.discordMessageSender.send(message.channelId, response);
                            });
                    })
                    .catch((e) => {
                        console.log(e);
                        this.logger.error(`[play] Unable to Join Voice Channel`);
                        const response = this.discordMessageFactory.buildMessage();
                        response.setDescription(
                            `Bot was unable to join ${voiceChannelName} to play podcast`,
                        );
                        this.discordMessageSender.send(message.channelId, response);
                    });
            })
            .catch(() => {
                this.logger.error(`[play] Unable to Process Podcast`);
                const response = this.discordMessageFactory.buildMessage();
                response.setDescription(`There was a problem with the selected podcast feed.`);
                this.discordMessageSender.send(message.channelId, response);
            });
    }

    // TODO: Can we do better?
    system(message: IncomingMessage) {
        const guildId = message.guildId;

        switch (message.arguments[0]) {
            case 'prefix':
                if (message.fromServerManager && guildId) {
                    this.discordDataStorage.setPrefix(guildId, message.arguments[1] || '!');
                } else {
                    this.sendInadequatePermissionsMessage(message);
                }
            default:
                const helpMessage = this.discordMessageFactory.buildHelpMessage(guildId);
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

    // TODO: This should be simplified with the simple message sending.
    sendInadequatePermissionsMessage(message: IncomingMessage) {
        const permissionsMessage = this.discordMessageFactory.buildInadequatePermissionsMessage();
        this.discordMessageSender.send(message.channelId, permissionsMessage);
    }

    podcastHasLatestEpisode(podcast: Podcast): boolean {
        const guid = this.podcastDataStorage.getPostedFromUrl(podcast.feed);
        return podcast.episodeList.reduce((accumulator: boolean, current: PodcastEpisode) => {
            return accumulator || current.guid == guid;
        }, false);
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
