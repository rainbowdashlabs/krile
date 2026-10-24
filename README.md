# Krile

Krile is a Discord bot to manage publicly provided tags.
Those tags are provided by git repositories.

Servers can import the repositories they need and make the tags inside them available to their community.

Documentation: https://docs.chojo.dev/krile

- [Importing an existing repository](https://docs.chojo.dev/krile/repositories/add)
- [Setting up a repository](https://docs.chojo.dev/krile/repositories/create)
- [Setting up a tag](https://docs.chojo.dev/krile/tags)

## Use it

Invite the [public instance](https://discord.com/oauth2/authorize?client_id=1126568385732419615&scope=bot&permissions=274877959168).

## Run it yourself

You need Docker and a PostgreSQL database. The image is `ghcr.io/rainbowdashlabs/krile`: `latest` is the newest
release, `dev` follows the `main` branch.

[`compose.yml`](compose.yml) starts the bot together with PostgreSQL. The bot reads `config/config.json` and
`config/log4j2.xml` from the mounted `config` folder.

## Build

Java 25 is required.

```bash
./gradlew build
```

The runnable jar is `build/libs/krile-<version>-all.jar`. Start it with
`java -Dbot.config=config/config.json -jar krile-<version>-all.jar`.

## Licence

[AGPL-3.0-only](LICENSE.md)
