import {
    AudioPlayerStatus,
    createAudioPlayer,
    createAudioResource,
    VoiceConnection,
} from '@discordjs/voice';
import { RESOLVER } from 'awilix';
import { Logger } from 'log4js';
import { PodcastEpisode } from '../../models/podcast-episode.js';
import { DiscordMessageSender } from '../discord/discord-message-sender.js';

export class AudioPlayPodcast {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    discordMessageSender: DiscordMessageSender;
    logger: Logger;
    constructor(discordMessageSender: DiscordMessageSender, logger: Logger) {
        this.discordMessageSender = discordMessageSender;
        this.logger = logger;
    }

    play(podcastEpisode: PodcastEpisode, voiceConnection: VoiceConnection) {
        const player = createAudioPlayer();
        const resource = createAudioResource(podcastEpisode.audio);
        voiceConnection.subscribe(player);

        player.play(resource);
        player.on('error', (e) => {
            this.logger.debug('[Play Audio] Errored', { podcastEpisode });
            voiceConnection.destroy();
        });
        player.on(AudioPlayerStatus.Idle, () => {
            this.logger.debug('[Play Audio] Completed', { podcastEpisode });
            voiceConnection.destroy();
        });
    }
}
