import { REST as DiscordRest } from '@discordjs/rest';
import awilix from 'awilix';
import axios from 'axios';
import bettersqlite3 from 'better-sqlite3';
import { Routes as DiscordRoutes } from 'discord-api-types/v8';
import { Client as DiscordClient, Intents as DiscordIntents } from 'discord.js';
import { config as configDotenv } from 'dotenv';
import * as htmlparser2 from 'htmlparser2';
import log4js from 'log4js';
import TurndownService from 'turndown';
import { URL } from 'url';
import { Config } from './models/config.js';

export class Container {
    container: awilix.AwilixContainer;

    constructor(environmentValue: string = '') {
        this.container = awilix.createContainer();

        // Create config model
        configDotenv();
        const configModel: Config = new Config();
        configModel.appName = process.env.npm_package_name || '';
        configModel.appVersion = process.env.npm_package_version || '';

        switch (environmentValue) {
            case 'dev':
                configModel.discordBotToken = process.env.DISCORD_BOT_TOKEN_DEV || '';
                configModel.discordClientId = process.env.DISCORD_CLIENT_ID_DEV || '';
                break;
            default:
                configModel.discordBotToken = process.env.DISCORD_BOT_TOKEN_PROD || '';
                configModel.discordClientId = process.env.DISCORD_CLIENT_ID_PROD || '';
                break;
        }

        // Create Discord client
        const discordClient = new DiscordClient({
            intents: [DiscordIntents.FLAGS.GUILDS, DiscordIntents.FLAGS.GUILD_VOICE_STATES],
            presence: {
                status: 'online',
                activities: [
                    {
                        name: 'Slash Commands',
                        type: 'LISTENING',
                        url: 'https://github.com/jimlind/discord.podcasts',
                    },
                ],
            },
        });

        // Create logger
        log4js.configure({
            appenders: {
                log_to_file: {
                    type: 'file',
                    filename: 'log/application.log',
                    maxLogSize: 10485760,
                    backups: 3,
                    compress: true,
                },
                out: { type: 'stdout' },
            },
            categories: {
                default: { appenders: ['log_to_file', 'out'], level: 'debug' },
            },
        });
        const logger = log4js.getLogger();
        const turndownService = new TurndownService();

        this.container.register({
            axios: awilix.asValue(axios),
            betterSqlite3: awilix.asValue(bettersqlite3),
            config: awilix.asValue(configModel),
            discordClient: awilix.asValue(discordClient),
            discordRest: awilix.asValue(DiscordRest),
            discordRoutes: awilix.asValue(DiscordRoutes),
            htmlParser2: awilix.asValue(htmlparser2),
            logger: awilix.asValue(logger),
            magicPrefix: awilix.asValue('?podcasts'),
            turndownService: awilix.asValue(turndownService),
        });
    }

    register(): Promise<awilix.AwilixContainer> {
        // Load all other modules
        return this.container.loadModules(['services/**/*.{ts,js}'], {
            formatName: 'camelCase',
            resolverOptions: {
                lifetime: awilix.Lifetime.SINGLETON,
                injectionMode: awilix.InjectionMode.CLASSIC,
            },
            cwd: new URL('.', import.meta.url).pathname,
            esModules: true,
        });
    }

    resolve<Type>(connectionString: string): Type {
        return this.container.resolve(connectionString);
    }
}
