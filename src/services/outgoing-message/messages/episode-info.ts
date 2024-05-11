import { EmbedBuilder } from 'discord.js';
import normalizeUrlFunction from 'normalize-url';
import { Episode, Podcast } from 'podparse';
import prettyMillisecondsFunction from 'pretty-ms';
import PodcastHelpers from '../../podcast/podcast-helpers.js';
import OutgoingMessageHelpers from '../outgoing-message-helpers.js';

interface EpisodeInfoInterface {
    readonly normalizeUrl: typeof normalizeUrlFunction;
    readonly outgoingMessageHelpers: OutgoingMessageHelpers;
    readonly podcastHelpers: PodcastHelpers;
    readonly prettyMilliseconds: typeof prettyMillisecondsFunction;

    build(embedBuilder: EmbedBuilder, podcast: Podcast): EmbedBuilder;
}

export default class EpisodeInfo implements EpisodeInfoInterface {
    constructor(
        readonly normalizeUrl: typeof normalizeUrlFunction,
        readonly outgoingMessageHelpers: OutgoingMessageHelpers,
        readonly podcastHelpers: PodcastHelpers,
        readonly prettyMilliseconds: typeof prettyMillisecondsFunction,
    ) {}

    public build(embedBuilder: EmbedBuilder, podcast: Podcast): EmbedBuilder {
        const episode = this.podcastHelpers.getMostRecentPodcastEpisode(podcast);
        const footerText = this.footerText(episode);
        let url;
        try {
            url = this.normalizeUrl(podcast.meta.link || podcast.meta.showUrl || '');
        } catch (error) {
            // Do nothing. Default to undefined.
        }

        embedBuilder.setAuthor({
            name: podcast.meta.title,
            iconURL: podcast.meta.image?.url || undefined,
            url,
        });
        embedBuilder.setTitle(episode.title || '-');
        embedBuilder.setURL(episode.link || null);
        embedBuilder.setDescription(
            this.outgoingMessageHelpers.compressEpisodeDescription(
                episode.description || episode.title || '-',
            ),
        );
        embedBuilder.setImage(episode.image?.url || podcast.meta.image?.url || null);
        if (footerText) {
            embedBuilder.setFooter({ text: footerText });
        }

        return embedBuilder;
    }

    private footerText(episode: Episode): string {
        const footerData = [];

        let episodeText = '';
        episodeText += episode.season ? `S${episode.season}` : '';
        episodeText += episode.season && episode.episode ? ':' : '';
        episodeText += episode.episode ? `E${episode.episode}` : '';
        footerData.push(episodeText);

        const duration = Number(episode.duration);
        if (duration) {
            footerData.push(this.prettyMilliseconds(episode.duration * 1000));
        }

        footerData.push(episode.explicit ? 'Parental Advisory - Explicit Content' : '');

        return footerData.filter(Boolean).join(' | ');
    }
}
