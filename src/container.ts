import * as awilix from 'awilix';
import * as axios from 'axios';
import { Client as DiscordClient } from 'discord.js';
import { config as configDotenv } from 'dotenv';
import * as htmlparser2 from 'htmlparser2';
import { Config } from './models/config';

export class Container {
    container: awilix.AwilixContainer;

    constructor() {
        this.container = awilix.createContainer();

        // Create config model
        configDotenv();
        const configModel: Config = new Config();
        configModel.discordBotToken = process.env.DISCORD_BOT_TOKEN || '';

        this.container.register({
            axios: awilix.asValue(axios),
            config: awilix.asValue(configModel),
            discordClient: awilix.asClass(DiscordClient).classic(),
            htmlParser2: awilix.asValue(htmlparser2),
        });

        // Load all other modules
        this.container.loadModules(['services/**/*.ts'], {
            formatName: 'camelCase',
            resolverOptions: {
                lifetime: awilix.Lifetime.SINGLETON,
                injectionMode: awilix.InjectionMode.CLASSIC,
            },
            cwd: __dirname,
        });
    }

    resolve<Type>(connectionString: string): Type {
        return this.container.resolve(connectionString);
    }
}
