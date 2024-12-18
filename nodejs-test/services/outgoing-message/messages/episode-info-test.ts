import { randomUUID } from 'crypto';
import { EmbedBuilder } from 'discord.js';
import { Episode, Meta, MetaImage, Podcast } from 'podparse';
import sinon from 'sinon';
import EpisodeInfo from '../../../../src/services/outgoing-message/messages/episode-info.js';
import OutgoingMessageHelpers from '../../../../src/services/outgoing-message/outgoing-message-helpers.js';
import PodcastHelpers from '../../../../src/services/podcast/podcast-helpers.js';

describe('Episode Info Class', function () {
    describe('build Method', function () {
        it('should set the title of the embed', async function () {
            const title = randomUUID();

            const mockNormalizeUrl = sinon.stub();
            const mockOutgoingMessageHelpers = sinon.createStubInstance(OutgoingMessageHelpers);
            const mockPodcastHelpers = sinon.createStubInstance(PodcastHelpers);
            const mockPrettyMilliseconds = sinon.stub();
            const episodeInfo = new EpisodeInfo(
                mockNormalizeUrl,
                mockOutgoingMessageHelpers,
                mockPodcastHelpers,
                mockPrettyMilliseconds,
            );

            const mockEmbed = sinon.createStubInstance(EmbedBuilder);
            const image = <MetaImage>{};
            const meta = <Meta>{ title, image };
            const episode = <Episode>{ title };

            const podcast = <Podcast>{ meta, episodes: [] };

            mockPodcastHelpers.getMostRecentPodcastEpisode.returns(episode);

            episodeInfo.build(mockEmbed, podcast);

            sinon.assert.calledWith(mockEmbed.setTitle, title);
        });
    });
});
