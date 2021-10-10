import { RESOLVER } from 'awilix';
import TurndownService from 'turndown';
import { PodcastFeedRow } from '../../models/db/podcast-feed-row';

export class OutgoingMessageHelpers {
    static [RESOLVER] = {};

    turndownService: TurndownService;
    constructor(turndownService: TurndownService) {
        this.turndownService = turndownService;
    }

    feedRowsToGridString(rows: PodcastFeedRow[]): string {
        const gridRows: Array<string> = ['ID     / TITLE'];
        rows.forEach((row) => {
            gridRows.push(`${row.id} / ${row.title}`);
        });
        return gridRows.join('\n');
    }

    compressPodcastDescription(podcastDescription: string): string {
        const markdown = this._convertTextToMarkdown(podcastDescription);
        // Consolidate multiple new lines by replacing duplicate whitespace characters with empty string
        return markdown.replace(/^\s*\n/gm, '');
    }

    compressEpisodeDescription(episodeDescription: string): string {
        const markdown = this._convertTextToMarkdown(episodeDescription);
        // Match all text up to the next new line
        return markdown.match(/[^\n\r]*/)?.shift() || '';
    }

    _convertTextToMarkdown(input: string): string {
        const stringWithOnlyHtmlBreaks = input.trim().replace(/[\r\n]+/g, '<br>');
        return this.turndownService.turndown(stringWithOnlyHtmlBreaks);
    }
}
