# API Stress Test

App that helps you perform stress test of your APIs, or just run some requests.

The app can either be run as a java app or an electron app, find instructions below.

## Environment variables

- `PORT`: port on which to run the app (default: `8083`)
- `PROFILE`: spring profile to apply (default: `prod`)
    - `dev`: in-memory DB is used
    - `prod`: prod DB is used
- `DB_USER`: Database user (default: `postgres.rsuhstqfoaoabfzgezxr`)
- `DB_PASSWORD`: Database password (default: `<empty>`)
- `DB_PORT`: Database port (default: `6543`)
- `OAUTH_CLIENT_ID`: google OAuth client ID (default: `null`)
- `OAUTH_CLIENT_SECRET`: google OAuth client secret (default: `null`)
- `DEV_REFRESH_TOKEN`: google refresh token, optional, only for `dev` profile (default: `null`)
- `ENCRYPTION_KEY`: secret key to encrypt/decrypt sensitive data, e.g. cookies (default: `null`)
    - format: base64 AES-256 key
    - how to generate: `openssl rand -base64 32`

## Run as java app

```shell
mvn clean package
java -jar target/app.jar
```

## Run as electron app (macOS)

### Pre-requisites

- `.api-stress-test.env` file is present in the user's root directory (`~/.api-stress-test.env`)
  containing all necessary environment variables

### Generate icon from png

Only necessary when re-generating the app icon.

```shell
# Install if needed
brew install imagemagick
brew install iconutil

mkdir icon.iconset
sips -z 128 128 icon.png --out icon.iconset/icon_128x128.png
iconutil -c icns icon.iconset
```

### Compile java app

```shell
npm run prepare-jar
```

### Run app from project directory

```shell
npm run start
```

### Package macOS app

```shell
npm run package
```

Then, install it as a normal `dmg` app.

## Notes

- `CASCADE` constraints do not work properly for nested records in `H2` database (dev profile),
  therefore deleting
  folders fails. I do not care enough to fix this anytime soon.
- how to generate a slim JRE (using the java version in your machine) to be shipped with the
  electron app:

```shell
rm -rf jre
jlink \
  --add-modules java.base,java.desktop,java.logging,java.sql,java.xml,java.naming,java.management,jdk.unsupported,java.security.jgss,java.instrument,jdk.crypto.ec,jdk.zipfs \
  --bind-services \
  --strip-debug \
  --compress=2 \
  --no-header-files \
  --no-man-pages \
  --output jre
```
