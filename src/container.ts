import log4js from 'log4js';
import awilix from 'awilix';
import axios from 'axios';
import { Client as DiscordClient } from 'discord.js';
import { config as configDotenv } from 'dotenv';
import * as htmlparser2 from 'htmlparser2';
import sqlite3 from 'sqlite3';
import { URL } from 'url';
import { Config } from './models/config.js';

export class Container {
    container: awilix.AwilixContainer;

    constructor() {
        this.container = awilix.createContainer();

        // Create config model
        configDotenv();
        const configModel: Config = new Config();

        switch (process.argv[2]) {
            case 'dev':
                configModel.discordBotToken = process.env.DISCORD_BOT_TOKEN_DEV || '';
                break;
            default:
                configModel.discordBotToken = process.env.DISCORD_BOT_TOKEN_PROD || '';
                break;
        }

        // Create Discord client
        const client = new DiscordClient({
            presence: {
                status: 'online',
                activity: {
                    name: '`?podcasts` command',
                    type: 'LISTENING',
                },
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

        this.container.register({
            axios: awilix.asValue(axios),
            config: awilix.asValue(configModel),
            discordClient: awilix.asValue(client),
            htmlParser2: awilix.asValue(htmlparser2),
            logger: awilix.asValue(logger),
            sqlite3: awilix.asValue(sqlite3.verbose()),
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
