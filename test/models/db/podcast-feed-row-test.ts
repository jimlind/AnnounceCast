import assert from 'assert';
import { randomUUID } from 'crypto';
import PodcastFeedRow from '../../../src/models/db/podcast-feed-row.js';

describe('Podcast Feed Row Model', function () {
    describe('constructor Method', function () {
        it('should save constructor inputs to public attributes', async function () {
            const id = randomUUID();
            const url = randomUUID();
            const title = randomUUID();

            const row = new PodcastFeedRow(id, url, title);

            assert.strictEqual(id, row.id);
            assert.strictEqual(url, row.url);
            assert.strictEqual(title, row.title);
        });
    });
});
