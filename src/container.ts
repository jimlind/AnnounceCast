import awilix from 'awilix';
import axios from 'axios';
import bettersqlite3 from 'better-sqlite3';
import {
    ActivityType as DiscordActivityType,
    Client as DiscordClient,
    EmbedBuilder as DiscordEmbedBuilder,
    Events as DiscordEvents,
    GatewayIntentBits as DiscordGatewayIntentBits,
    GuildMember as DiscordGuildMember,
    REST as DiscordRest,
    Routes as DiscordRoutes,
} from 'discord.js';
import exitHook from 'exit-hook';
import log4js from 'log4js';
import normalizeUrl from 'normalize-url';
import getPodcastFromFeed from 'podparse';
import prettyMilliseconds from 'pretty-ms';
import TurndownService from 'turndown';
import { URL } from 'url';
import config from './config.js';
import * as constants from './constants.js';

export class Container {
    container: awilix.AwilixContainer;

    constructor() {
        this.container = awilix.createContainer();

        // Create Discord Rest
        const discordRest = new DiscordRest().setToken(config.discord.botToken);

        // Create Discord Routes
        const discordCommandRoutes = DiscordRoutes.applicationCommands(config.discord.clientId);

        // Create Discord Client
        const discordClient = new DiscordClient({
            intents: [DiscordGatewayIntentBits.Guilds],
            presence: {
                status: 'online',
                activities: [
                    {
                        name: 'Slash Commands',
                        type: DiscordActivityType.Listening,
                        url: 'https://jimlind.github.io/AnnounceCast/',
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
        const turndownService = new TurndownService({ hr: '\n' });

        this.container.register({
            axios: awilix.asValue(axios),
            betterSqlite3: awilix.asValue(bettersqlite3),
            config: awilix.asValue(config),
            constants: awilix.asValue(constants),
            discordClient: awilix.asValue(discordClient),
            discordCommandRoutes: awilix.asValue(discordCommandRoutes),
            discordEmbedBuilder: awilix.asValue(DiscordEmbedBuilder),
            discordEvents: awilix.asValue(DiscordEvents),
            discordGuildMember: awilix.asValue(DiscordGuildMember),
            discordRest: awilix.asValue(discordRest),
            exitHook: awilix.asValue(exitHook),
            getPodcastFromFeed: awilix.asValue(getPodcastFromFeed),
            logger: awilix.asValue(logger),
            normalizeUrl: awilix.asValue(normalizeUrl),
            prettyMilliseconds: awilix.asValue(prettyMilliseconds),
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
