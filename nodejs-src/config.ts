import dotenv from 'dotenv';
// It is frowned up on to have multiple .env files but I'm going for it anyway
dotenv.config({ path: `.env.${process.env.NODE_ENV}` });

interface Config {
    app: {
        name: string;
        version: string;
    };
    discord: {
        botToken: string;
        clientId: string;
    };
}

const config: Config = {
    app: {
        name: process.env.npm_package_name || '',
        version: process.env.npm_package_version || '',
    },
    discord: {
        botToken: process.env.DISCORD_BOT_TOKEN || '',
        clientId: process.env.DISCORD_CLIENT_ID || '',
    },
};

export default config;
