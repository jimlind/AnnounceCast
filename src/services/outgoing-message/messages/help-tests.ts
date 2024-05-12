import { EmbedBuilder } from 'discord.js';
import OutgoingMessageHelpers from '../outgoing-message-helpers.js';

interface HelpTestsInterface {
    readonly outgoingMessageHelpers: OutgoingMessageHelpers;

    build(embedBuilderFunction: () => EmbedBuilder): EmbedBuilder[];
}

export default class HelpTests implements HelpTestsInterface {
    constructor(readonly outgoingMessageHelpers: OutgoingMessageHelpers) {}

    public build(embedBuilderFunction: () => EmbedBuilder): EmbedBuilder[] {
        const result = [];

        const introText =
            "This is the first of a series of test messages. If you can see this you know that basic command edit embeds work.\nIf you don't see any of the following messages you need to update your permissions as documented.\nNext you should see a basic embed message.";
        result.push(embedBuilderFunction().setDescription(introText));

        const basicText =
            'This is a basic embed message.\nNext you should see an embed with a simple emoji.';
        result.push(embedBuilderFunction().setDescription(basicText));

        const simpleEmojiText =
            'This is a embed message with simple emoji :dog::cat::mouse:.\nNext you should see an embed with formatted text.';
        result.push(embedBuilderFunction().setDescription(simpleEmojiText));

        const formattedText =
            'This is a embed message *with* **formatted** ***text***.\nNext you should see an embed with an image.';
        result.push(embedBuilderFunction().setDescription(formattedText));

        const imageText =
            'This is a embed message with an image.\nThis concludes this test of the Emergency Broadcast System..';
        const imageURL = 'https://placebear.com/300/300';
        result.push(embedBuilderFunction().setDescription(imageText).setThumbnail(imageURL));

        return result;
    }
}
