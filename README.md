# GemMod

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen.svg)](https://github.com/HoneyBerries/GemMod/releases)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/HoneyBerries/GemMod/actions)

A feature-rich Minecraft plugin for Paper/Folia 1.21.5+ that introduces magical gems with unique abilities for players. Perfect for SMPs and custom servers seeking new gameplay mechanics!

## Table of Contents

- [Features](#features)
- [Gems](#gems)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Upcoming Gems](#upcoming-gems)
- [Supported Platforms](#supported-platforms)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

## Features

- Custom crafting recipes for each gem
- Cooldown system and permission-based bypass
- Highly configurable and designed for performance (Folia supported)

## Gems

- **Air Gem**: Double jump with a powerful velocity boost
- **Darkness Gem**: Become invisible and hide your equipment from others
- **Earth Gem**: Gain temporary invulnerability with maximum resistance
- **Fire Gem**: Launch explosive fireballs
- **Light Gem**: Strike players with lightning and see others glowing through walls

## Installation

> **Note:** GemMod is very new and there is currently **no public download available**. You must build the plugin yourself from source. If you need help, feel free to ask in our [Discord](https://discord.com/invite/3W5GQ37h)!

1. Download and install the latest [PacketEvents plugin](https://modrinth.com/plugin/packetevents) (required dependency). Place it in your server's `plugins` folder.
2. Clone the repository:
   ```sh
   git clone https://github.com/HoneyBerries/GemMod.git
   cd GemMod
   ```
3. Build with Maven:
   ```sh
   mvn clean package
   ```
4. Place the generated `GemMod.jar` from the `target/` folder into your server's `plugins` directory.
5. Start or reload your server.
6. Configure settings in `data.yml` as needed.

## Commands

- `/gem <gem-type> <player (optional)> <amount (optional)>`
  - Give gems to yourself or other players.
  - Permission: `gemmod.command.gem`

## Permissions

- `gemmod.command.gem` — Use the `/gem` command (default: OP)
- `gemmod.cooldown.bypass` — Bypass gem ability cooldowns (default: false)

## Upcoming Gems

GemMod is actively developed! Here are some gems planned for future updates:

- **Ice Gem**: Freeze water, slow down enemies, or create ice paths
- **Water Gem**: Breathe underwater, swim faster, or control water flows
- More unique gems and abilities are in the works—suggest your ideas on our [Discord](https://discord.com/invite/3W5GQ37h)!

## Supported Platforms

- **Paper** 1.21.5+
- **Folia** 1.21.5+

## Building from Source

1. Clone the repository:
   ```sh
   git clone https://github.com/HoneyBerries/GemMod.git
   cd GemMod
   ```
2. Build with Maven:
   ```sh
   mvn clean package
   ```
3. The plugin JAR will be in the `target/` directory.

## Contributing

Pull requests and suggestions are welcome! Please open an issue or PR on [GitHub](https://github.com/HoneyBerries/GemMod).

## License

This project is licensed under the Apache 2.0 License. See [LICENSE](LICENSE) for details.

---

*Created with ❤️ by [HoneyBerries](https://github.com/HoneyBerries)*
