import { RESOLVER } from 'awilix';
import { Logger } from 'log4js';
import { Podcast } from '../models/podcast';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage';
import { DiscordMessageFactory } from './discord/discord-message-factory';
import { DiscordMessageSender } from './discord/discord-message-sender';

export class Bot {
    static [RESOLVER] = {};

    discordMessageFactory: DiscordMessageFactory;
    discordMessageSender: DiscordMessageSender;
    postcastDataStorage: PodcastDataStorage;
    logger: Logger;

    constructor(
        discordMessageFactory: DiscordMessageFactory,
        discordMessageSender: DiscordMessageSender,
        podcastDataStorage: PodcastDataStorage,
        logger: Logger,
    ) {
        this.discordMessageFactory = discordMessageFactory;
        this.discordMessageSender = discordMessageSender;
        this.postcastDataStorage = podcastDataStorage;
        this.logger = logger;
    }

    writePodcastToChannelList(podcast: Podcast, channelList: Array<string>) {
        const message = this.discordMessageFactory.build(podcast);

        channelList.forEach((channelId: string) => {
            this.discordMessageSender.send(channelId, message).then(() => {
                this.postcastDataStorage.updatePostedData(podcast.showFeed, podcast.episodeGuid);
                this.logger.info(`Message sent: ${message.author?.name} -- ${message.title}`);
            });
        });
    }

    podcastIsLatest(podcast: Podcast): boolean {
        const guid = this.postcastDataStorage.getPostedFromUrl(podcast.showFeed);

        return podcast.episodeGuid == guid;
    }
}
