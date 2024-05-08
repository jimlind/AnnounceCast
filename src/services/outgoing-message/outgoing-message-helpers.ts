import TurndownService from 'turndown';
import PodcastFeedRow from '../../models/db/podcast-feed-row.js';

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

    public feedRowsToGridString(rows: PodcastFeedRow[], truncate: boolean = false): string {
        if (truncate && rows.length > 20) {
            rows = rows.slice(0, 21);
            rows[20].id = '...   ';
            rows[20].title = '...';
        }

        const gridRows: Array<string> = ['ID     / TITLE'];
        rows.forEach((row) => {
            gridRows.push(`${row.id} / ${row.title.slice(0, 36)}`);
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
        const text = markdown.match(/[^\n\r]*/)?.shift() || '';
        return text.length > 1024 ? text.substring(0, 1024).trim() + '...' : text;
    }

    private convertTextToMarkdown(input: string): string {
        if (typeof input !== 'string') {
            return '';
        }

        const stringWithOnlyHtmlBreaks = input.trim().replace(/[\r\n]+/g, '<br>');
        return this.turndownService.turndown(stringWithOnlyHtmlBreaks);
    }
}
