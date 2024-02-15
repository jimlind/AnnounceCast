import TurndownService from 'turndown';
import PodcastFeedRow from '../../models/db/podcast-feed-row';

interface OutgoingMessageHelpersInterface {
    turndownService: TurndownService;

    feedRowsToGridString(rows: PodcastFeedRow[]): string;
    compressPodcastDescription(podcastDescription: string): string;
    compressEpisodeDescription(episodeDescription: string): string;
}

export default class OutgoingMessageHelpers implements OutgoingMessageHelpersInterface {
    turndownService: TurndownService;
    constructor(turndownService: TurndownService) {
        this.turndownService = turndownService;
    }

    public feedRowsToGridString(rows: PodcastFeedRow[]): string {
        const gridRows: Array<string> = ['ID     / TITLE'];
        rows.forEach((row) => {
            gridRows.push(`${row.id} / ${row.title}`);
        });
        return gridRows.join('\n');
    }

    public compressPodcastDescription(podcastDescription: string): string {
        const markdown = this.convertTextToMarkdown(podcastDescription);
        // Consolidate multiple new lines by replacing duplicate whitespace characters with empty string
        return markdown.replace(/^\s*\n/gm, '');
    }

    public compressEpisodeDescription(episodeDescription: string): string {
        const markdown = this.convertTextToMarkdown(episodeDescription);
        // Match all text up to the next new line
        return markdown.match(/[^\n\r]*/)?.shift() || '';
    }

    private convertTextToMarkdown(input: string): string {
        const stringWithOnlyHtmlBreaks = input.trim().replace(/[\r\n]+/g, '<br>');
        return this.turndownService.turndown(stringWithOnlyHtmlBreaks);
    }
}
