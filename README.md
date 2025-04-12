# API Stress Test

User-friendly app to perform stress test of your APIs.

## Environment variables

- `PORT`: port on which to run the app (default: `8083`)
- `PROFILE`: spring profile to apply (default: `prod`)
    - `dev`: in-memory DB is used
    - `prod`: prod DB is used
- `DB_PASSWORD`: database password (default: `null`)
- `OAUTH_CLIENT_ID`: OAuth client ID (default: `null`)
- `OAUTH_CLIENT_SECRET`: OAuth client secret (default: `null`)
- `DEV_REFRESH_TOKEN`: google refresh token, optional, only for `dev` profile (default: `null`)
- `ENCRYPTION_KEY`: secret key to encrypt/decrypt sensitive data, e.g. cookies (default: `null`)
    - format: base64 AES-256 key
    - how to generate: `openssl rand -base64 32`

## Run production build locally

```shell
mvn clean package -Pproduction
```

Then, start the app with `prod` spring profile.