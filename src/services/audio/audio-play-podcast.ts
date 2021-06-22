import { RESOLVER } from 'awilix';
import { VoiceConnection } from 'discord.js';
import { Logger } from 'log4js';
import { Podcast } from '../../models/podcast.js';
import { DiscordMessageSender } from '../discord/discord-message-sender.js';

export class AudioPlayPodcast {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    discordMessageSender: DiscordMessageSender;
    logger: Logger;
    constructor(discordMessageSender: DiscordMessageSender, logger: Logger) {
        this.discordMessageSender = discordMessageSender;
        this.logger = logger;
    }

    play(podcast: Podcast, voiceConnection: VoiceConnection, channelId: string) {
        const info = `${podcast.title} in ${voiceConnection.channel.name}`;
        this.logger.debug(`[Play Audio] Attempting ${info}`);

        voiceConnection
            .play(podcast.episodeList[0].audio)
            .on('start', () => {
                this.logger.debug(`[Play Audio] Started ${info}`);
                this.discordMessageSender.sendString(channelId, `Started playing ${info}`);
            })
            .on('finish', () => {
                this.logger.debug(`[Play Audio] Completed ${info}`);
                voiceConnection.channel.leave();
            })
            .on('error', (e: Error) => {
                this.logger.debug(`[Play Audio] Errored ${info} : ${e.message}`);
                this.discordMessageSender.sendString(channelId, 'Unknown error occured.');
            });
    }
}
