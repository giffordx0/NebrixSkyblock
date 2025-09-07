# Nebrix Skyblock (Internal)

Core engine for Nebrix's private Skyblock server. This is an internal plugin; behavior is opinionated and configuration is minimal.

## Setup
1. Build with Java 17: `./gradlew shadowJar`
2. Drop `build/libs/nebrix-skyblock-0.0.1-all.jar` into your Paper 1.21 server's `plugins` folder.
3. Start the server once to generate `config.yml`. Adjust numeric tables as desired (generators, limits, values, upgrades, boosters, missions).

## Commands
- `/is` — open the main island menu.

## Permissions
- `nebrix.admin` — full administrative access.

## Extension Points
- Other plugins may access the island service via `NebrixSkyblockAPI`.

This plugin intentionally keeps non-numeric settings in code to avoid configuration sprawl.
