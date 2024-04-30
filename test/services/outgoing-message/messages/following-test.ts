import assert from 'assert';
import { randomUUID } from 'crypto';
import { EmbedBuilder } from 'discord.js';
import sinon from 'sinon';
import PodcastFeedRow from '../../../../src/models/db/podcast-feed-row.js';
import Following from '../../../../src/services/outgoing-message/messages/following.js';
import OutgoingMessageHelpers from '../../../../src/services/outgoing-message/outgoing-message-helpers.js';

describe('Following Class', function () {
    describe('build Method', function () {
        it('should set the title of the embed', async function () {
            const mockedOutgoingMessageHelpers = sinon.createStubInstance(OutgoingMessageHelpers);
            const mockEmbed = sinon.createStubInstance(EmbedBuilder);

            const following = new Following(mockedOutgoingMessageHelpers);
            following.build(mockEmbed, []);

            sinon.assert.calledWith(mockEmbed.setTitle, 'Podcasts Followed in this Channel');
        });

        it('should translate row input to description', async function () {
            const mockedOutgoingMessageHelpers = sinon.createStubInstance(OutgoingMessageHelpers);
            const mockEmbed = sinon.createStubInstance(EmbedBuilder);
            const rowList = [new PodcastFeedRow(randomUUID(), randomUUID(), randomUUID())];
            const gridString = randomUUID();

            mockedOutgoingMessageHelpers.feedRowsToGridString.returns(gridString);

            const following = new Following(mockedOutgoingMessageHelpers);
            following.build(mockEmbed, rowList);

            sinon.assert.calledOnceWithExactly(
                mockedOutgoingMessageHelpers.feedRowsToGridString,
                rowList,
            );
            sinon.assert.calledOnceWithExactly(
                mockEmbed.setDescription,
                '```\n' + gridString + '\n```',
            );
        });

        it('should return the same embed builder from input', async function () {
            const mockedOutgoingMessageHelpers = sinon.createStubInstance(OutgoingMessageHelpers);
            const mockEmbed = sinon.createStubInstance(EmbedBuilder);

            const following = new Following(mockedOutgoingMessageHelpers);
            const buildReturn = following.build(mockEmbed, []);

            assert.strictEqual(mockEmbed, buildReturn);
        });
    });
});
