interface PodcastFeedRowInterface {
    id: string;
    title: string;
}

export default class PodcastFeedRow implements PodcastFeedRowInterface {
    constructor(
        public id: string,
        public title: string,
    ) {}
}
