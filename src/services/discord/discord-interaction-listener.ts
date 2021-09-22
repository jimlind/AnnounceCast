import { RESOLVER } from 'awilix';
import { GuildMember } from 'discord.js';
import { DiscordConnection } from './discord-connection';

export class DiscordInteractionListener {
    static [RESOLVER] = {}; // So Awilix autoloads the class
    MESSAGE_ACTION_KEY = 'message';

    discordConnection: DiscordConnection;

    constructor(discordConnection: DiscordConnection) {
        this.discordConnection = discordConnection;
    }

    onInteraction(callback: Function) {
        // Get connected client and listen for interactions
        return this.discordConnection.getConnectedClient().then((client) => {
            client.on('interactionCreate', (interaction) => {
                // Ignore if not a command and not from a guild member
                if (!interaction.isCommand()) return;
                if (!(interaction.member instanceof GuildMember)) return;

                // Immediatly set interaction status to defer reply (creates a ... status to edit)
                return interaction.deferReply().then(() => {
                    callback(interaction);
                });
            });
        });
    }
}
