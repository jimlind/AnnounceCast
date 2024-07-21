import awilix from 'awilix';
import axios from 'axios';
import bettersqlite3 from 'better-sqlite3';
import * as chrono from 'chrono-node';
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
import truncateMarkdown from 'markdown-truncate';
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

        // A logger layout that ignores anything that isn't the first argument
        log4js.addLayout('simple', () => {
            return (event) => `${event?.startTime} ${event?.level?.levelStr}  ${event?.data?.[0]}`;
        });

        // A logger layout that writes things to standard JSON blobs
        log4js.addLayout('json', () => {
            const replacer = (key: string, value: unknown) =>
                typeof value === 'bigint' ? value.toString() : value;
            return (event) =>
                JSON.stringify(
                    {
                        timestamp: event?.startTime,
                        message: event?.data?.[0],
                        ...event,
                        data: event?.data?.[1],
                    },
                    replacer,
                );
        });

        // Create logger
        log4js.configure({
            appenders: {
                log_to_file: {
                    type: 'file',
                    filename: 'log/application.log',
                    mode: 0o644,
                    maxLogSize: 10485760,
                    backups: 3,
                    compress: true,
                    layout: { type: 'json' },
                },
                out: { type: 'stdout', layout: { type: 'simple' } },
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
            chrono: awilix.asValue(chrono),
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
            truncateMarkdown: awilix.asValue(truncateMarkdown),
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
