import { AxiosResponse } from 'axios';
import { randomUUID } from 'crypto';
import sinon from 'sinon';
import * as Constants from '../../../src/constants.js';
import HttpClient from '../../../src/services/http-client.js';
import PodcastDataStorage from '../../../src/services/podcast/podcast-data-storage.js';
import PodcastHelpers from '../../../src/services/podcast/podcast-helpers.js';

describe('Podcast Helpers Class', function () {
    describe('getPodcastFromUrl Method', function () {
        it('should make http client request for feed using the input url', async function () {
            const mockedConstants = <typeof Constants>{
                ERRORS: { NO_PODCAST_EPISODES_FOUND_MESSAGE: randomUUID() },
            };
            const mockedGetPodcastFromFeed = sinon.stub().returns({ meta: {} });
            const mockedHttpClient = sinon.createStubInstance(HttpClient);
            const mockedPodcastDataStorage = sinon.createStubInstance(PodcastDataStorage);

            const podcastHelpers = new PodcastHelpers(
                mockedConstants,
                mockedGetPodcastFromFeed,
                mockedHttpClient,
                mockedPodcastDataStorage,
            );

            const feedUrl = randomUUID();
            await podcastHelpers.getPodcastFromUrl(feedUrl);
            sinon.assert.calledWith(mockedHttpClient.get, feedUrl, 5000);
        });

        it('should parse podcast with response from http client request', async function () {
            const mockedConstants = <typeof Constants>{
                ERRORS: { NO_PODCAST_EPISODES_FOUND_MESSAGE: randomUUID() },
            };
            const mockedGetPodcastFromFeed = sinon.stub().returns({ meta: {} });
            const mockedHttpClient = sinon.createStubInstance(HttpClient);
            const mockedPodcastDataStorage = sinon.createStubInstance(PodcastDataStorage);

            const responseData = randomUUID();
            const axiosResponse = <AxiosResponse>{ data: responseData };
            const axiosPromise: Promise<AxiosResponse> = new Promise((resolve) =>
                resolve(axiosResponse),
            );
            (mockedHttpClient.get = sinon.stub()).returns(axiosPromise);

            const podcastHelpers = new PodcastHelpers(
                mockedConstants,
                mockedGetPodcastFromFeed,
                mockedHttpClient,
                mockedPodcastDataStorage,
            );

            await podcastHelpers.getPodcastFromUrl(randomUUID());
            sinon.assert.calledWith(mockedGetPodcastFromFeed, responseData);
        });
    });
});
