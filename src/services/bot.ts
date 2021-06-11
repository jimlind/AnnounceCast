import { RESOLVER } from 'awilix';
import { Logger } from 'log4js';
import { Message } from '../models/message';
import { Podcast } from '../models/podcast';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage';
import { DiscordDataStorage } from './discord/discord-data-storage';
import { DiscordMessageFactory } from './discord/discord-message-factory';
import { DiscordMessageSender } from './discord/discord-message-sender';
import { PodcastProcessor } from './podcast/podcast-processor';

export class Bot {
    static [RESOLVER] = {};

    discordMessageFactory: DiscordMessageFactory;
    discordMessageSender: DiscordMessageSender;
    discordDataStorage: DiscordDataStorage;
    podcastDataStorage: PodcastDataStorage;
    podcastProcessor: PodcastProcessor;
    logger: Logger;

    constructor(
        discordMessageFactory: DiscordMessageFactory,
        discordMessageSender: DiscordMessageSender,
        discordDataStorage: DiscordDataStorage,
        podcastDataStorage: PodcastDataStorage,
        podcastProcessor: PodcastProcessor,
        logger: Logger,
    ) {
        this.discordMessageFactory = discordMessageFactory;
        this.discordMessageSender = discordMessageSender;
        this.discordDataStorage = discordDataStorage;
        this.podcastDataStorage = podcastDataStorage;
        this.podcastProcessor = podcastProcessor;
        this.logger = logger;
    }

    actOnUserMessage(message: Message) {
        switch (message.command) {
            case 'following':
                this.following(message);
                break;
            case 'follow':
                if (message.manageServer) {
                    this.follow(message);
                } else {
                    this.writeInadequatePermissionsMessage(message);
                }
                break;
            case 'unfollow':
                if (message.manageServer) {
                    this.unfollow(message);
                } else {
                    this.writeInadequatePermissionsMessage(message);
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

    follow(message: Message) {
        message.arguments.forEach((feedUrl: string) => {
            this.podcastProcessor
                .process(feedUrl)
                .then((podcast: Podcast) => {
                    this.podcastDataStorage.addFeed(podcast, message.channelId).then((feedList) => {
                        const followingMessage =
                            this.discordMessageFactory.buildFollowingMessage(feedList);

                        this.discordMessageSender
                            .send(message.channelId, followingMessage)
                            .then(() => {
                                this.logger.info(
                                    `Message Sent: Follow ${podcast.showTitle} on Channel ${message.channelId}`,
                                );
                            })
                            .catch((error: string) => {
                                this.logger.error(
                                    `Unable to Send Follows Message on ${message.channelId} [${error}]`,
                                );
                            });
                    });
                })
                .catch(() => {
                    this.logger.info(`Unable to Follow Podcast ${feedUrl}`);
                });
        });
    }

    unfollow(message: Message) {
        message.arguments.forEach((feedId: string) => {
            this.podcastDataStorage.removeFeed(feedId, message.channelId).then((feedList) => {
                const followingMessage = this.discordMessageFactory.buildFollowingMessage(feedList);

                this.discordMessageSender
                    .send(message.channelId, followingMessage)
                    .then(() => {
                        this.logger.info(
                            `Message Sent: Unfollowed ${feedId} on Channel ${message.channelId}`,
                        );
                    })
                    .catch((error: string) => {
                        this.logger.error(
                            `Unable to Send Unfollowed Message on ${message.channelId} [${error}]`,
                        );
                    });
            });
        });
    }

    following(message: Message) {
        this.podcastDataStorage.getFeedsByChannelId(message.channelId).then((feedList) => {
            const followingMessage = this.discordMessageFactory.buildFollowingMessage(feedList);

            this.discordMessageSender
                .send(message.channelId, followingMessage)
                .then(() => {
                    this.logger.info(`Message Sent: All Follows on Channel ${message.channelId}`);
                })
                .catch((error: string) => {
                    this.logger.error(
                        `Unable to Send All Follows Message on ${message.channelId} [${error}]`,
                    );
                });
        });
    }

    play(message: Message) {
        if (!message.voiceChannel) return;
        const voiceChannel = message.voiceChannel;

        this.podcastDataStorage.getFeedUrlByFeedId(message.arguments[0]).then((feedUrl) => {
            const podcast = this.podcastProcessor
                .process(feedUrl)
                .then((podcast) => {
                    voiceChannel
                        .join()
                        .then((connection) => {
                            connection
                                .play(podcast.showAudio)
                                .on('finish', () => {
                                    voiceChannel.leave();
                                })
                                .on('error', (error) =>
                                    this.logger.error(`Unable to Play Podcast [${error}]`),
                                );
                        })
                        .catch(() => {
                            this.logger.error(`Unable to Join Voice Channel`);
                        });
                })
                .catch(() => {
                    this.logger.error(`Unable to Process Podcast`);
                });
        });
    }

    system(message: Message) {
        const guildId = message.guildId;

        switch (message.arguments[0]) {
            case 'prefix':
                if (message.manageServer && guildId) {
                    this.discordDataStorage.setPrefix(guildId, message.arguments[1] || '!');
                } else {
                    this.writeInadequatePermissionsMessage(message);
                }
            default:
                const helpMessage = this.discordMessageFactory.buildHelpMessage(guildId);
                this.discordMessageSender.send(message.channelId, helpMessage);
                break;
        }
    }

    writePodcastAnnouncement(podcast: Podcast) {
        const message = this.discordMessageFactory.buildEpisodeMessage(podcast);

        this.podcastDataStorage.getChannelsByFeedUrl(podcast.showFeed).then((channelList) => {
            channelList.forEach((channelId: string) => {
                this.discordMessageSender
                    .send(channelId, message)
                    .then(() => {
                        this.podcastDataStorage.updatePostedData(
                            podcast.showFeed,
                            podcast.episodeGuid,
                        );
                        this.logger.info(
                            `Message Sent: ${message.author?.name} -- ${message.title}`,
                        );
                    })
                    .catch((error: string) => {
                        this.logger.error(
                            `Unable to Send Podcast Announcment ${message.title} on ${channelId} [${error}]`,
                        );
                    });
            });
        });
    }

    writeInadequatePermissionsMessage(message: Message) {
        const permissionsMessage = this.discordMessageFactory.buildInadequatePermissionsMessage();
        this.discordMessageSender.send(message.channelId, permissionsMessage);
    }

    podcastIsLatest(podcast: Podcast): boolean {
        const guid = this.podcastDataStorage.getPostedFromUrl(podcast.showFeed);

        return podcast.episodeGuid == guid;
    }
}
