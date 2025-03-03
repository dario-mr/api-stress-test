# API Stress Test

User-friendly app to perform stress test of your APIs.

## Environment variables

- `PORT`: port on which to run the app (default: `8083`)
- `PROFILE`: spring profile to apply (default: `prod`)
    - `dev`: in-memory DB is used
    - `prod`: prod DB is used
- `OAUTH_CLIENT_ID`: OAuth client ID (default: `null`)
- `OAUTH_CLIENT_SECRET`: OAuth client secret (default: `null`)

## Generate production build locally

```shell
mvn clean package -Pproduction
```

Then, start the app with `prod` spring profile.