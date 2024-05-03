import assert from 'assert';
import config from '../src/config.js';

// These are bare minimum tests. Not great because of the logic that runs when importing config
describe('Config Model', function () {
    it('should pick up values from process env', async function () {
        assert.strictEqual(process.env.npm_package_name, config.app.name);
        assert.strictEqual(process.env.npm_package_version, config.app.version);
    });
    it('should pick up empty strings from unset env values', async function () {
        assert.strictEqual('', config.discord.botToken);
        assert.strictEqual('', config.discord.clientId);
    });
});
