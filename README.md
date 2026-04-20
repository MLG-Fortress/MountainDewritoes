# MountainDewritoes
Custom server plugin for the MLG Fortress Minecraft server.

IP: `mlg.robomwm.com`

Some features include:

### [Message bubbles (click to watch):](http://www.youtube.com/watch?v=UDQeqLJ3jik)

[![Message Bubbles demonstration video](http://img.youtube.com/vi/UDQeqLJ3jik/0.jpg)](http://www.youtube.com/watch?v=UDQeqLJ3jik)

- Hitmarkers
- Hitsounds
- Semi-keepinv
- Low health sound and visual effects
- Have you noticed a lot of FPS elements in here?
- Eliminations
- Health canisters
- Health pickups
- ArmorAugmentation (armor does stuff)
- Voice lines
- Tip notifications
- Transaction notifications
- Nicknames
- Name colors
- Warps
- Quick access menu
- Changelog
- Schedule server restarts after plugin updates
- Others
- Seriously I can't seem to stop myself from adding more in here

## What is in this repo
This is a long-running "all-in-one" server plugin. It mixes gameplay features, quality-of-life systems, commands, and integrations used by the MLG Fortress network.

The plugin entry point is:
- `me.robomwm.MountainDewritoes.MountainDewritoes`

Feature code is mostly grouped by package:
- `combat/` and `combat/twoshot/` - combat tuning and weapon systems
- `Sounds/` - hit/low-health/replacement sound behavior
- `armor/` - armor augmentation logic
- `Music/` - atmospheric/music systems
- `Commands/` - command handlers and command utilities
- `notifications/` - tips and transaction/player notifications
- `spaceship/`, `arena/`, `NotOverwatch/`, `Rewards/`, `hotmenu/` - gameplay systems and experiments
- `Events/` and `packetwrappers/` - custom event plumbing and packet wrappers

There is also a `trash/` folder containing older or unused classes kept for reference.

## Requirements
- Java 21
- Paper API `26.1.2+` (as declared in `pom.xml`)
- Maven 3.x

## Build
```bash
mvn clean package
```

The built plugin jar is shaded and produced under `target/`.

## Runtime integrations
MountainDewritoes supports many optional plugins through soft dependencies and runtime checks. Notable integrations include:
- CustomItemRegistry
- GrandioseAPI
- SimpleClans
- BetterTPA
- Vault
- ProtocolLib
- PrettySimpleShop
- ServerListPlus

Behavior for several features is enabled only when corresponding plugins are present.

## Commands
Registered commands (see `plugin.yml`) include:
- `nick` (aliases: `color`, `name`, `nickname`, ...)
- `warp`
- `mdebug`
- `restart`, `restartnow`, `update`
- `clearchat`
- `tip`
- `voice`
- `start`
- `music`
- `shrug`
- `lejail`
- `reset`
- `md`
- `watchwinreward`
- `newlog`, `deletelog`
- `info`
- `settings`
- `view`
- `changelog`
- `login`
- `syncconfigs`
- `mop`

## Notes
- This project is intentionally practical rather than flashy.
- The codebase has evolved over many years, so package names and features reflect both active systems and historical experiments.

## Disabled features
- View-distance changing via `/view` is currently disabled (the command currently sends guidance text only).
