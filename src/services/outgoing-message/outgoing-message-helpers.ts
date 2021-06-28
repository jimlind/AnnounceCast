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
        const descriptionWithHtmlBreaks = podcastDescription.replace(/[\r\n]+/g, '<br>');
        const descriptionMarkdown = this.turndownService.turndown(descriptionWithHtmlBreaks);
        const descriptionMarkdownWithSingleNewLine = descriptionMarkdown.replace(/^\s*\n/gm, '');

        return descriptionMarkdownWithSingleNewLine;
    }
}
