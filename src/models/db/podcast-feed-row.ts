interface PodcastFeedRowInterface {
    id: string;
    url: string;
    title: string;
}

export default class PodcastFeedRow implements PodcastFeedRowInterface {
    constructor(
        public id: string,
        public url: string,
        public title: string,
    ) {}
}
