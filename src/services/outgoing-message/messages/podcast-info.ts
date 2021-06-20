import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Podcast } from '../../../models/podcast';
import TurndownService from 'turndown';

export class PodcastInfo {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    turndownService: TurndownService;
    constructor(turndownService: TurndownService) {
        this.turndownService = turndownService;
    }

    build(message: MessageEmbed, podcast: Podcast): MessageEmbed {
        message.setTitle(podcast.title);
        message.setThumbnail(podcast.image);
        message.setDescription(this._getDescription(podcast));
        message.setFooter(`Credit: ${podcast.author}`);

        return message;
    }

    _getDescription(podcast: Podcast): string {
        const descriptionWithHtmlBreaks = podcast.description.replace(/[\r\n]+/g, '<br>');
        const descriptionMarkdown = this.turndownService.turndown(descriptionWithHtmlBreaks);
        const descriptionMarkdownWithSingleNewLine = descriptionMarkdown.replace(/^\s*\n/gm, '');

        return (
            descriptionMarkdownWithSingleNewLine +
            '\n\n' +
            `Show Feed URL: ${podcast.feed}` +
            '\n' +
            `Show's Website: ${podcast.link}`
        );
    }
}
