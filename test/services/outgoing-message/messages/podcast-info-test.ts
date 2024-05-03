import { randomUUID } from 'crypto';
import { EmbedBuilder } from 'discord.js';
import { Meta, MetaImage, Podcast } from 'podparse';
import sinon from 'sinon';
import PodcastInfo from '../../../../src/services/outgoing-message/messages/podcast-info.js';
import OutgoingMessageHelpers from '../../../../src/services/outgoing-message/outgoing-message-helpers.js';

describe('Podcast Info Class', function () {
    describe('build Method', function () {
        it('should set the title of the embed', async function () {
            const title = randomUUID();

            const mockedOutgoingMessageHelpers = sinon.createStubInstance(OutgoingMessageHelpers);
            const podcastInfo = new PodcastInfo(mockedOutgoingMessageHelpers);

            const mockEmbed = sinon.createStubInstance(EmbedBuilder);
            const image = <MetaImage>{ link: '', title: '' };
            const meta = <Meta>{ title, image };
            const podcast = <Podcast>{ meta, episodes: [] };

            podcastInfo.build(mockEmbed, podcast);

            sinon.assert.calledOnceWithExactly(mockEmbed.setTitle, title);
        });
    });
});
