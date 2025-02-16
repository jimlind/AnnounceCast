The bot runs directly on a VPS for simplicity. No containers or shenanigans.
This is documented in the RUNNING document.
I use a GitHub Workflow that has access to some environment variables and credentials that are stored as Repository
secrets to download the latest release and restart the server.

The workflow is in .github/workflows/run-deploy.yml

It was pretty straight forward.
I generated the ssh key and I don't think I needed to use my github email address but it felt right and it did work
`> ssh-keygen -t ed25519 -f ~/.ssh/id_ed25519 -C "email-address-on-github-account"`

When I attempted to run it as-is I got errors about lack of connectivity.
I updated my ssh config to allow connection via key and set this new key as an allowed key.
Then when I ran the Workflow again it worked.
