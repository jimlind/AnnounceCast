import { randomUUID } from 'crypto';
import { Client, EmbedBuilder } from 'discord.js';
import sinon from 'sinon';
import config from '../../../../src/config.js';
import DiscordConnection from '../../../../src/services/discord/discord-connection.js';
import Help from '../../../../src/services/outgoing-message/messages/help.js';
import PodcastDataStorage from '../../../../src/services/podcast/podcast-data-storage.js';

describe('Help Class', function () {
    describe('build Method', function () {
        it('should set the title of the embed', async function () {
            const name = randomUUID();
            const version = randomUUID();
            const setTitle = `${name} v${version} Documentation`;

            const mockConfig = <typeof config>{ app: { name, version } };
            const mockDiscordConnection = sinon.createStubInstance(DiscordConnection);
            const mockPodcastDataStorage = sinon.createStubInstance(PodcastDataStorage);
            const help = new Help(mockConfig, mockDiscordConnection, mockPodcastDataStorage);

            const mockClient = <Client<true>>{ guilds: { cache: { size: 99 } } };
            const clientPromise: Promise<Client<true>> = new Promise((resolve) =>
                resolve(mockClient),
            );
            mockDiscordConnection.getClient.returns(clientPromise);

            const mockEmbed = sinon.createStubInstance(EmbedBuilder);

            await help.build(mockEmbed);

            sinon.assert.calledWith(mockEmbed.setTitle, setTitle);
        });
    });
});
