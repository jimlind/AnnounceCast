import assert from 'assert';
import { randomUUID } from 'crypto';
import sinon from 'sinon';
import TurndownService from 'turndown';
import OutgoingMessageHelpers from '../../../src/services/outgoing-message/outgoing-message-helpers.js';

describe('Outgoing Message Helpers Class', function () {
    describe('formatPodcastDescription Method', function () {
        it('should pass short string through private methods', async function () {
            const truncate = sinon.stub();
            const turndown = sinon.createStubInstance(TurndownService);
            const outgoingMessageHelpers = new OutgoingMessageHelpers(truncate, turndown);
            outgoingMessageHelpers['convertHtmlToMarkdown'] = (input: string) => input;
            outgoingMessageHelpers['shortenMarkdown'] = (input: string) => input;

            const htmlText = randomUUID();
            const result = outgoingMessageHelpers.formatPodcastDescription(htmlText);

            assert.strictEqual(result, htmlText);
        });
    });

    describe('formatEpisodeDescription Method', function () {
        it('should pass short string through private methods', async function () {
            const truncate = sinon.stub();
            const turndown = sinon.createStubInstance(TurndownService);
            const outgoingMessageHelpers = new OutgoingMessageHelpers(truncate, turndown);
            outgoingMessageHelpers['convertHtmlToMarkdown'] = (input: string) => input;
            outgoingMessageHelpers['shortenMarkdown'] = (input: string) => input;

            const htmlText = randomUUID();
            const result = outgoingMessageHelpers.formatEpisodeDescription(htmlText);

            assert.strictEqual(result, htmlText);
        });

        it('should cut off episode description after line break', async function () {
            const truncate = sinon.stub();
            const turndown = sinon.createStubInstance(TurndownService);
            const outgoingMessageHelpers = new OutgoingMessageHelpers(truncate, turndown);
            outgoingMessageHelpers['convertHtmlToMarkdown'] = (input: string) => input;
            outgoingMessageHelpers['shortenMarkdown'] = (input: string) => input;

            const firstString = randomUUID();
            const secondString = randomUUID();
            const fullInput = firstString + '\n' + secondString;
            const result = outgoingMessageHelpers.formatEpisodeDescription(fullInput);

            assert.strictEqual(result, firstString);
        });
    });

    describe('shortenMarkdown Method', function () {
        it('should count link formatting characters towards limit', async function () {
            const truncate = sinon.stub();
            const turndown = sinon.createStubInstance(TurndownService);
            const outgoingMessageHelpers = new OutgoingMessageHelpers(truncate, turndown);
            const shortedReference = outgoingMessageHelpers['shortenMarkdown'];

            const link = '[America Online is the best service provider.](https://www.aol.com)';
            const inputText = randomUUID() + link.repeat(2) + randomUUID();
            truncate.returns(inputText);

            const truncateCall1 = truncate.withArgs(inputText, { limit: 128, ellipsis: true });
            // The formatting for the link created above is 46 characters long
            const truncateCall2 = truncate.withArgs(inputText, { limit: 128 - 46, ellipsis: true });

            shortedReference.call(outgoingMessageHelpers, inputText, 128);

            sinon.assert.calledTwice(truncate);
            sinon.assert.callOrder(truncateCall1, truncateCall2);
        });
    });
});
