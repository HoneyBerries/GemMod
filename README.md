# GemMod

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/HoneyBerries/GemMod/actions)

A feature-rich Minecraft plugin for Paper/Folia 1.21.7+ that introduces magical gems with unique abilities. Written in pure Java and built with Gradle. **Requires [PacketEvents](https://modrinth.com/plugin/packetevents)**.

---

## Table of Contents

- [Features](#features)
- [Gem Descriptions](#gem-descriptions)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Upcoming Gems](#upcoming-gems)
- [Supported Platforms](#supported-platforms)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- Adds special gem items to Minecraft, each granting unique abilities:
  - **Air Gem:** Double jump (velocity boost)
  - **Darkness Gem:** Temporary invisibility and hides equipment
  - **Earth Gem:** Temporary invulnerability (resistance effect)
  - **Fire Gem:** Launches a powerful fireball
  - **Light Gem:** Strikes a player with lightning and makes others glow

---

## Gem Descriptions

- **Air Gem**  
  Double jump with a powerful velocity boost.

- **Darkness Gem**  
  Become invisible and hide your equipment from other players.

- **Earth Gem**  
  Gain temporary invulnerability with maximum resistance.

- **Fire Gem**  
  Launch explosive fireballs.

- **Light Gem**  
  Strike players with lightning and see others glowing through walls.

---

## Installation

> **Note:** There is currently **no public download**. You must build GemMod from source.

1. **Install [PacketEvents](https://modrinth.com/plugin/packetevents):**  
   Download the latest PacketEvents jar and place it in your server's `plugins` folder.

2. **Clone the GemMod repository:**
   ```sh
   git clone https://github.com/HoneyBerries/GemMod.git
   cd GemMod
   ```

3. **Build the plugin using Gradle:**
   ```sh
   ./gradlew build
   ```
   The compiled jar will be in `build/libs/`.

4. **Place the GemMod jar in your server's `plugins` folder.**

5. **Restart your server.**

---

## Commands

```sh
/gem <gem-type> <player (optional)> <amount (optional)>   # Give a player a specific gem
/gem help                                                # Show help for /gem
/gemmod reload                                           # Reload plugin configuration and recipes
/gemmod help                                             # Show help for /gemmod
```

---

## Permissions

| Permission                | Description                                 | Default |
|---------------------------|---------------------------------------------|---------|
| gemmod.command.gem        | Allow use of `/gem`                         | op      |
| gemmod.command.gemmod     | Allow use of `/gemmod`                      | op      |
| gemmod.cooldown.bypass    | Bypass gem ability cooldowns                | false   |

---

## Upcoming Gems

- **Water Gem:** Prevent opponents from moving and swim insanely fast.
- **Ice Gem:** Freeze players and create ice paths.
- *And more planned!*

---

## Supported Platforms

- **Minecraft:** Paper 1.21.7+ / Folia 1.21.7+
- **Java:** 21+
- **Dependencies:** [PacketEvents](https://modrinth.com/plugin/packetevents)

---

## Building from Source

1. **Clone the repository:**
   ```sh
   git clone https://github.com/HoneyBerries/GemMod.git
   cd GemMod
   ```

2. **Build with Gradle:**
   ```sh
   ./gradlew build
   ```
   The plugin jar will be in `build/libs/`.

---

## Contributing

Contributions are welcome! Please open issues or pull requests on [GitHub](https://github.com/HoneyBerries/GemMod).

---

## License

GemMod is licensed under the [Apache 2.0 License](LICENSE).
