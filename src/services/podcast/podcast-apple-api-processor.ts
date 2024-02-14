import PodcastHelpers from './podcast-helpers';

interface PodcastAppleAPIProcessorInterface {
    readonly httpClient: import('../http-client').default;
    readonly podcastHelpers: PodcastHelpers;

    search(searchTerm: string, podcastCount: number): Promise<import('podparse').Podcast[]>;
}

export default class PodcastAppleAPIProcessor implements PodcastAppleAPIProcessorInterface {
    constructor(
        readonly httpClient: import('../http-client').default,
        readonly podcastHelpers: PodcastHelpers,
    ) {}

    async search(searchTerm: string, podcastCount: number): Promise<import('podparse').Podcast[]> {
        const count = Math.max(podcastCount, 10); // Too few requested and this fails oddly, so minimum 10
        const uri = `https://itunes.apple.com/search?term=${searchTerm}&country=US&media=podcast&attribute=titleTerm&limit=${count}`;
        const encodedUri = encodeURI(uri);
        const response = await this.httpClient.get(encodedUri, 5000);

        // Exit early if the results are not an array
        if (!Array.isArray(response?.data?.results)) {
            return [];
        }

        const results: import('podparse').Podcast[] = [];
        for (const result of response.data.results) {
            // Some podcasts don't have an RSS feed, we ignore them because we can't scrape them.
            if (!result.feedUrl || !result.feedUrl.includes('http')) {
                continue;
            }

            const podcast = await this.podcastHelpers.getPodcastFromUrl(result.feedUrl);
            results.push(podcast);

            // Stop at the input count if we've reached it
            if (results.length == podcastCount) {
                break;
            }
        }
        return results;
    }
}
