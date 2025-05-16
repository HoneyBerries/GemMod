# GemMod

**GemMod** is a Minecraft plugin that introduces powerful elemental gems, each granting unique abilities and passive effects to players. Enhance your gameplay with new mechanics, strategic combat, and rare crafting challenges.

---

## Features

- **Air Gem**: Dash and immunity to fall/fly-into-wall damage.
- **Darkness Gem**: Temporary invisibility and blinding attacks.
- **Earth Gem**: Grants Haste, Speed, Strength, and temporary invulnerability.
- **Fire Gem**: Launches powerful fireballs and grants fire resistance.
- **Light Gem**: Strikes targets with lightning and reveals all players with a glowing outline.
- **Ice & Water Gems**: (Planned/expand as needed)
- **Unique Crafting**: Each gem can only be crafted once per server.
- **Cooldowns**: Abilities have configurable cooldowns, with action bar feedback.
- **Admin Commands**: Give gems, reload config, and more.

---

## Installation

1. Download the latest release of GemMod.
2. Place the `.jar` file into your server's `plugins` directory.
3. Restart the server.

---

## Usage

- **Crafting**: Gems are crafted using special recipes. Once crafted, the recipe is removed and cannot be used again.
- **Abilities**: Right-click with a gem in your main hand to activate its ability (if available).
- **Passive Effects**: Simply keep the gem in your inventory to benefit from its passive powers.

---

## Commands

| Command                              | Description                                 | Permission                   |
|---------------------------------------|---------------------------------------------|------------------------------|
| `/gem <gem-type> [player] [amount]`   | Give a gem to a player                      | `honeyberries.command.gem`   |
| `/gem reload`                         | Reload plugin configuration and recipes      | `honeyberries.command.gem`   |
| `/gem help`                           | Show help message                           | `honeyberries.command.gem`   |

---

## Configuration

Gem crafting status is tracked in `data.yml`. Each gem can only be crafted once per server. To reset crafting, edit or delete `data.yml` and reload the plugin.

---

## Development

- Built for PaperMC/Folia 1.21.4+ (update as needed)

---

## Contributing

Contributions, suggestions, and bug reports are welcome! Please open an issue or pull request on GitHub.

---

## Credits

- Plugin by HoneyBerries
- Art by Pepmon270

---

*Good luck, adventurer!*