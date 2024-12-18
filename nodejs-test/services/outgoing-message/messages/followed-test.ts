import { randomUUID } from 'crypto';
import { EmbedBuilder } from 'discord.js';
import { Meta, MetaImage, Podcast } from 'podparse';
import sinon from 'sinon';
import Followed from '../../../../src/services/outgoing-message/messages/followed.js';
import OutgoingMessageHelpers from '../../../../src/services/outgoing-message/outgoing-message-helpers.js';

describe('Followed Class', function () {
    describe('build Method', function () {
        it('should set the title of the embed', async function () {
            const title = randomUUID();
            const setTitle = `You are now following ${title}`;

            const mockOutgoingMessageHelpers = sinon.createStubInstance(OutgoingMessageHelpers);
            const followed = new Followed(mockOutgoingMessageHelpers);

            const mockEmbed = sinon.createStubInstance(EmbedBuilder);
            const image = <MetaImage>{ link: '', title: '' };
            const meta = <Meta>{ title, image };
            const podcast = <Podcast>{ meta, episodes: [] };

            followed.build(mockEmbed, podcast, []);

            sinon.assert.calledOnceWithExactly(mockEmbed.setTitle, setTitle);
        });
    });
});
