import { randomUUID } from 'crypto';
import { Logger } from 'log4js';
import sinon from 'sinon';
import * as Constants from '../../../src/constants.js';
import PodcastDataStorage from '../../../src/services/podcast/podcast-data-storage.js';
import PodcastFetch from '../../../src/services/podcast/podcast-fetch.js';
import PodcastHelpers from '../../../src/services/podcast/podcast-helpers.js';

describe('Podcast Helpers Class', function () {
    describe('getPodcastFromUrl Method', function () {
        it('should make podcast fetch request for feed using the input url', async function () {
            const mockedConstants = <typeof Constants>{
                ERRORS: { NO_PODCAST_EPISODES_FOUND_MESSAGE: randomUUID() },
            };
            const mockedGetPodcastFromFeed = sinon.stub().returns({ meta: {} });
            const mockedLogger = <Logger>{};
            const mockedPodcastDataStorage = sinon.createStubInstance(PodcastDataStorage);
            const mockedPodcastFetch = sinon.createStubInstance(PodcastFetch);

            mockedLogger.debug = sinon.stub();

            const responseData = randomUUID();
            const responsePromise: Promise<string> = new Promise((resolve) =>
                resolve(responseData),
            );
            (mockedPodcastFetch.getPartialPodcastStringFromUrl = sinon.stub()).returns(
                responsePromise,
            );

            const podcastHelpers = new PodcastHelpers(
                mockedConstants,
                mockedGetPodcastFromFeed,
                mockedLogger,
                mockedPodcastDataStorage,
                mockedPodcastFetch,
            );

            const feedUrl = randomUUID();
            await podcastHelpers.getPodcastFromUrl(feedUrl);
            sinon.assert.calledWith(
                mockedPodcastFetch.getPartialPodcastStringFromUrl,
                feedUrl,
                5000,
            );
        });

        it('should parse podcast with response from podcast fetch request', async function () {
            const mockedConstants = <typeof Constants>{
                ERRORS: { NO_PODCAST_EPISODES_FOUND_MESSAGE: randomUUID() },
            };
            const mockedGetPodcastFromFeed = sinon.stub().returns({ meta: {} });
            const mockedLogger = <Logger>{};
            const mockedPodcastDataStorage = sinon.createStubInstance(PodcastDataStorage);
            const mockedPodcastFetch = sinon.createStubInstance(PodcastFetch);

            mockedLogger.debug = sinon.stub();

            const responseData = randomUUID();
            const responsePromise: Promise<string> = new Promise((resolve) =>
                resolve(responseData),
            );
            (mockedPodcastFetch.getPartialPodcastStringFromUrl = sinon.stub()).returns(
                responsePromise,
            );

            const podcastHelpers = new PodcastHelpers(
                mockedConstants,
                mockedGetPodcastFromFeed,
                mockedLogger,
                mockedPodcastDataStorage,
                mockedPodcastFetch,
            );

            await podcastHelpers.getPodcastFromUrl(randomUUID());
            sinon.assert.calledWith(mockedGetPodcastFromFeed, responseData);
        });
    });
});
