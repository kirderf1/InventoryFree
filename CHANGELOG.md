# Changelog

All notable user-facing changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

- Update to Minecraft 1.21.3 / NeoForge 21.3

### Changed

- Change logger names to "inventory_free"

### Fixed

- Fix error that was logged on config unload

## [1.21.0-3.3.1] - 2024-08-04

- Update to Minecraft 1.21 / NeoForge 21.0 (also compatible with Minecraft 1.21.1)

## [1.20.6-3.3.1] - 2024-08-04

- Update to NeoForge 20.6.78-beta and later

## [1.20.6-3.3.0] - 2024-05-11

- Update to Minecraft 1.20.6 / NeoForge 20.6 beta

## [1.20.4-3.3.0] - 2024-02-03

- Update to Minecraft 1.20.4 / NeoForge 20.4

### Changed

- Data previously stored in player persistent data is now stored as neoforge data attachments

## [1.20.2-3.2.2] - 2023-12-05

- Update to NeoForge 20.2.86

## [1.20.2-3.2.1] - 2023-10-31

- Update to Minecraft 1.20.2 / NeoForge 20.2 beta

## [1.20.1-3.2.1] - 2023-07-22

### Fixed

- Fix lock texture being drawn over dragged item and over tooltips

## [1.20.1-3.2.0] - 2023-06-15

- Update to Minecraft 1.20.1 / MinecraftForge 47

## [1.19.4-3.2.0] - 2023-03-17

- Update to Minecraft 1.19.4 / MinecraftForge 45

## [1.19.3-3.2.0] - 2022-12-10

- Update to Minecraft 1.19.3 / MinecraftForge 44

### Added

- Configurable unlock cost multiplier
- Warning messages for invalid unlock item or unlock cost

## [1.19-3.1.2] - 2022-07-24

- Update to MinecraftForge 41.0.94+

## [1.19-3.1.1] - 2022-06-09

- Update to Minecraft 1.19 / MinecraftForge 41

## [1.18.2-3.1.1] - 2022-05-06

### Fixed

- Fix mod compability issue with certain inventory screens

## [1.18.2-3.1.0] - 2022-03-19

- Update to Minecraft 1.18.2 / MinecraftForge 40

## [1.17.1-3.0.2] - 2021-12-19

### Fixed

- Fix issue with certain data being lost on death (backport)

## [1.18.1-3.1.0] - 2021-12-19

### Added

- Configurable unlock cost progression with new options "linear" and "exponential"

### Fixed

- Fix issue with certain data being lost on death

## [1.18.1-3.0.1] - 2021-12-15

- Update to Minecraft 1.18.1 / MinecraftForge 39

## [1.18-3.0.1] - 2021-12-03

- Update to Minecraft 1.18 / MinecraftForge 38

## [1.17.1-3.0.1] - 2021-07-24

- Update to Minecraft 1.17.1 / MinecraftForge 37

## [1.16.5-3.0.1] - 2021-07-12

### Changed

- Slot unlock item tooltip text now has a translation key

### Fixed

- Fix sidedness crash on dedicated servers

## [1.16.5-3.0.0] - 2021-07-11

- Explicitly support Minecraft 1.16.5 / MinecraftForge 36

### Added

- A "locked inventory" where items are put when their original inventory slot becomes locked
- Config option for dropping items in locked slots

### Changed

- New icon for locked slots (provided by Winddbourne)

### Fixed

- Fixed slots not immediately being re-blocked client-side after a player respawn/dimension switch

## [1.15.2-2.1.0] - 2020-12-30

### Added

- Config option to choose how many slots are relocked on death (backport)

### Removed

- Config option `clearUnlockedOnDeath`, replaced by new option (backport)

## [1.16.4-2.1.0] - 2020-12-30

### Added

- Config option to choose how many slots are relocked on death

### Removed

- Config option `clearUnlockedOnDeath`, replaced by new option

## [1.16.4-2.0.0] - 2020-12-01

- Update to Minecraft 1.16.4 / MinecraftForge 35 (inherently compatible with Minecraft 1.16.5)

## [1.15.2-2.0.0] - 2020-11-30

- Update to Minecraft 1.15.2 / MinecraftForge 31

## [1.14.4-2.0.0] - 2020-11-30

### Added

- Each player now has an "unlocked slots" property
- Number of unlocked slots for a player can now be set per player by command
- Slots can be unlocked by using item specified in config
- Config option for clearing unlocked slots on death

## [1.14.4-1.1] - 2020-09-06

### Added

- Config option for number of available slots

## [1.14.4-1.0] - 2020-09-05

Also referred to as just "1.0"

### Added

- Overlay rendered on blocked slots
- Also override slots serverside

### Fixed

- Slot override fixes

## [1.14.4-0.1] - 2020-09-05

Also referred to as just "0.1"

- Initial release
