import { ChatInputCommandInteraction } from 'discord.js';

interface DiscordInteractionListenerInterface {
    readonly discordConnection: import('./discord-connection.js').default;
    readonly discordEvents: typeof import('discord.js').Events;

    startListeners(callback: (arg: ChatInputCommandInteraction) => void): void;
}

export default class DiscordInteractionListener implements DiscordInteractionListenerInterface {
    constructor(
        readonly discordConnection: import('./discord-connection.js').default,
        readonly discordEvents: typeof import('discord.js').Events,
    ) {}

    async startListeners(callback: (arg: ChatInputCommandInteraction) => void) {
        const discordClient = await this.discordConnection.getClient();
        discordClient.on(this.discordEvents.InteractionCreate, async (interaction) => {
            // Ignore if not an actual command
            if (!interaction.isChatInputCommand()) return;
            // Immediatly set interaction status to defer reply (creates a ... status to edit)
            await interaction.deferReply();
            // Call the method
            callback(interaction);
        });
    }
}
