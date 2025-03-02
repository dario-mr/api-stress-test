# API Stress Test

User-friendly app to perform stress test of your APIs.

## Environment variables

- `PORT`: port on which to run the app (default: `8083`)
- `PROFILE`: spring profile to apply (default: `prod`)
    - `dev`: vaadin production mode is off; in-memory DB is used
    - `prod`: vaadin production mode is on; prod DB is used
- `DB_PASSWORD`: database password (default: `null`)

## Generate production build locally

```shell
mvn clean package -Pproduction
```

Then, start the app normally.