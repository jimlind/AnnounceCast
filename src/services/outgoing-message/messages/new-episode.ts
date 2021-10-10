import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Podcast } from '../../../models/podcast';
import { PodcastEpisode } from '../../../models/podcast-episode';
import { OutgoingMessageHelpers } from '../outgoing-message-helpers';

export class NewEpisode {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    outgoingMessageHelpers: OutgoingMessageHelpers;
    constructor(outgoingMessageHelpers: OutgoingMessageHelpers) {
        this.outgoingMessageHelpers = outgoingMessageHelpers;
    }

    build(message: MessageEmbed, podcast: Podcast): MessageEmbed {
        const podcastEpisode = podcast.getFirstEpisode();

        message.setAuthor(podcast.title, podcast.image, podcast.link);
        message.setTitle(podcastEpisode.title);
        message.setURL(podcastEpisode.link);
        message.setDescription(
            this.outgoingMessageHelpers.compressEpisodeDescription(podcastEpisode.description),
        );
        message.setImage(podcastEpisode.image || podcast.image);

        message.setFooter(this._footerText(podcastEpisode));

        return message;
    }

    _footerText(podcastEpisode: PodcastEpisode): string {
        const footerData = [];

        let episodeText = '';
        episodeText += podcastEpisode.season ? `S${podcastEpisode.season}` : '';
        episodeText += podcastEpisode.season && podcastEpisode.number ? ':' : '';
        episodeText += podcastEpisode.number ? `E${podcastEpisode.number}` : '';
        footerData.push(episodeText);

        footerData.push(podcastEpisode.durationFormatted);
        footerData.push(podcastEpisode.explicit ? 'Parental Advisory - Explicit Content' : '');

        return footerData.filter(Boolean).join(' | ');
    }
}
