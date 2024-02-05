interface DiscordInteractionListenerInterface {
    readonly discordConnection: import('./discord-connection.js').default;
    readonly discordEvents: typeof import('discord.js').Events;

    startListeners(callback: Function): void;
}

export default class DiscordInteractionListener implements DiscordInteractionListenerInterface {
    constructor(
        readonly discordConnection: import('./discord-connection').default,
        readonly discordEvents: typeof import('discord.js').Events,
    ) {}

    async startListeners(callback: Function) {
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
