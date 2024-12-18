import TruncateMarkdown from 'markdown-truncate';
import TurndownService from 'turndown';
import PodcastFeedRow from '../../models/db/podcast-feed-row.js';

interface OutgoingMessageHelpersInterface {
    truncateMarkdown: typeof TruncateMarkdown;
    turndownService: TurndownService;

    feedRowsToGridString(rows: PodcastFeedRow[]): string;
    formatPodcastDescription(podcastDescription: string): string;
    formatEpisodeDescription(episodeDescription: string): string;
}

export default class OutgoingMessageHelpers implements OutgoingMessageHelpersInterface {
    constructor(
        readonly truncateMarkdown: typeof TruncateMarkdown,
        readonly turndownService: TurndownService,
    ) {}

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

    public formatPodcastDescription(podcastDescription: string): string {
        const markdown = this.convertHtmlToMarkdown(podcastDescription);

        return this.shortenMarkdown(markdown);
    }

    public formatEpisodeDescription(episodeDescription: string): string {
        const markdown = this.convertHtmlToMarkdown(episodeDescription);
        // Match all text up to the next new line
        const firstParagraph = markdown.match(/[^\n\r]*/)?.shift() || '';

        return this.shortenMarkdown(firstParagraph);
    }

    private convertHtmlToMarkdown(input: string): string {
        // Make all line breaks consistent
        const stringWithOnlyHtmlBreaks = input.trim().replace(/[\r\n]+/g, '<br>');
        const markdown = this.turndownService.turndown(stringWithOnlyHtmlBreaks);

        // Consolidate multiple new lines by replacing duplicate whitespace characters with empty string
        const cleanedLineBreaks = markdown.replace(/^\s*\n/gm, '');

        // Discord actively tries to prevent phishing attempts by not allowing the link text to look like a URL
        // To avoid how silly this looks we'll replace that markdown with just the URL
        const cleanedLinks = cleanedLineBreaks.replace(/\[http.*?\]\((.+?)\)/g, '$1');

        return cleanedLinks;
    }

    private shortenMarkdown(inputText: string, length: number = 1024): string {
        const options = { limit: length, ellipsis: true };
        let truncated = this.truncateMarkdown(inputText, options);

        // The truncateMarkdown method truncates for display text but we are more concerned with actual
        // character count because we can't send too many total characters in a message.
        if (truncated.length > length) {
            const matchedPartialLinkFormats = [...truncated.matchAll(/]\(.*?\)/g)];
            const linkFormatLength = matchedPartialLinkFormats.reduce((accumulator, current) => {
                return accumulator + current[0].length + 1;
            }, 0);
            options.limit = length > linkFormatLength ? length - linkFormatLength : 1;
            truncated = this.truncateMarkdown(inputText, options);
        }

        return truncated;
    }
}
