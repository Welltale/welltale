# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Welltale is a Hytale mod that implements an RPG system with characteristics, classes, spells, levels, and mobs. Built with Java 25 using the Hytale Modding API and Gradle with Kotlin DSL.

## Build & Development Commands

### Building
- `./gradlew build` - Build the project and run tests
- `./gradlew clean build` - Clean build directory and rebuild
- `./gradlew jar` - Build JAR file only

### Running
- `./gradlew runServer` - Start the Hytale server with the mod loaded
- `./gradlew server` - Alternative server start command

### Asset Management
- `./gradlew syncAssets` - Manually sync assets from build back to source
- **Auto-sync**: After `runServer` stops, assets are automatically synced from `build/resources/main` to `src/main/resources` (excluding manifest.json)

### Other Tasks
- `./gradlew tasks` - List all available tasks
- `./gradlew decompileServer` - Decompile Hytale server for development
- `./gradlew test` - Run tests (currently no tests exist)
- `./gradlew idea` - Generate IntelliJ IDEA project files

## Architecture

### Core Entry Point
- `Welltale.java` - Main plugin class that initializes all systems and repositories

### Modular Structure
Each game system follows a consistent pattern:
- **Data Models** - Domain objects (Player, Class, Mob, Rank, Spell)
- **Repositories** - In-memory JSON-based data storage
- **File Loaders** - JSON file loading with auto-creation of example files
- **Systems** - ECS (Entity Component System) processors
- **Events/Handlers** - Event-driven logic
- **UI Components** - Custom HUD and page builders

### Key Systems

**Characteristics System** (`fr.welltale.characteristic`)
- Manages player stats using Hytale's EntityStatMap with StaticModifiers
- Damage calculation with elemental resistances (Earth, Fire, Water, Air)
- DamageSystem applies Dofus-style combat mechanics:
  - Elemental boost: `damage * (1 + stat / 100)`
  - Resistance: `damage * (100 - resistance%) / 100` (capped at 50%)
  - Critical hit system with damage and resistance calculations
- MoveSpeedSystem updates player movement speed

**Level System** (`fr.welltale.level`)
- XP calculation using exponential curve: `A * level^2.8` where A=35
- Levels 1-200 supported
- PlayerLevelComponent attached to entities via custom component type
- OnDeathSystem grants XP to killer
- PlayerJoinSystem initializes new players
- XPTable handles all XP/level calculations

**Class System** (`fr.welltale.clazz`)
- Classes contain spell slugs and configuration
- Players select class via UI page
- ClassSelectPage for class selection UI

**Spell System** (`fr.welltale.spell`)
- SpellManager handles spell casting, cooldowns, and stamina costs
- Spells registered in spellRegistry (Jump, Supershot)
- CastSpellInteraction enables spell casting via item interactions
- SpellCooldownScheduler runs as background task

**Mob System** (`fr.welltale.mob`)
- Mobs have stats and damage characteristics
- MobStatsComponent attached to mob entities
- MobNameplateAssignSystem adds nameplates to mobs
- MobStatsAssignSystem initializes mob stats

**Player System** (`fr.welltale.player`)
- Player data stored in JSON at `./mods/welltale/players.json`
- Characteristics points system for stat allocation
- Event systems for player actions (blocks, items, chat)
- HUD updates via UpdatePlayerHudSystem

## File Structure

```
src/main/java/fr/welltale/
├── Welltale.java              # Main plugin entry point
├── characteristic/             # Stats and damage systems
│   ├── Characteristics.java   # Constants and stat management
│   └── system/                 # DamageSystem, MoveSpeedSystem
├── clazz/                      # Class system
│   ├── Class.java              # Class data model
│   └── event/                  # PlayerReadyEvent for class assignment
├── level/                      # XP and leveling
│   ├── PlayerLevelComponent.java # ECS component
│   ├── XPTable.java           # XP calculations
│   ├── event/                  # GiveXPEvent, LevelUpEvent
│   ├── handler/               # Event handlers
│   └── system/                 # OnDeathSystem, PlayerJoinSystem
├── mob/                        # Enemy mobs
│   ├── Mob.java               # Mob data model
│   ├── MobStatsComponent.java # ECS component
│   └── system/                 # Mob stat assignment
├── player/                     # Player management
│   ├── Player.java            # Player data model
│   ├── event/                  # Player events
│   └── system/                 # Player action systems
├── rank/                       # Player ranks
├── spell/                      # Spell casting
│   ├── SpellManager.java      # Spell registry and casting logic
│   ├── spells/                 # Individual spell implementations
│   └── CastSpellInteraction.java
└── hud/                        # Custom UI builders

src/main/resources/
├── manifest.json              # Plugin metadata (template with placeholders)
├── Common/UI/Custom/           # Custom UI definitions (.ui files)
│   ├── Hud/Player/            # Player HUD components
│   └── Pages/                  # Game pages (class selection, etc.)
├── Server/                     # Server-side resources
│   ├── Audio/                  # Sound events
│   ├── Entity/                 # Entity stats and damage causes
│   └── Item/                   # Items and interactions
```

## JSON Data Storage

All game data (players, ranks, classes, mobs) is stored as JSON files in `./mods/welltale/`:
- `players.json` - Player data with UUID, class, rank, characteristics, XP
- `ranks.json` - Rank configurations
- `classes.json` - Class definitions with spell slugs
- `mobs.json` - Mob stat configurations

**Important**: File loaders auto-create these files with example data if missing.

## ECS Integration

The mod heavily uses Hytale's Entity Component System:
- Components: `PlayerLevelComponent`, `MobStatsComponent`
- Systems: `EntityEventSystem` implementations
- Entity stat modifications via `EntityStatMap.putModifier()`
- Custom component types registered via `getEntityStoreRegistry().registerComponent()`

## Event System

- Event handlers registered via `getEventRegistry().registerGlobal()` or `register()`
- Custom events: `GiveXPEvent`, `LevelUpEvent`
- Hytale events: `PlayerConnectEvent`, `PlayerReadyEvent`, `PlayerChatEvent`

## UI Development

- Custom UI defined in `.ui` files using Hytale UI format
- UI builders in Java code (e.g., `PlayerHudBuilder`, `ClassSelectPage`)
- HUD components use custom textures from `Common/UI/Custom/`

## Configuration

- `gradle.properties` - Plugin metadata and server version
- `build.gradle.kts` - Build configuration, dependencies, task definitions
- Server version must match Hytale server version

## Dependencies

- Jackson for JSON processing (Kotlin module + databind)
- Lombok for code generation (compile-only)
- Hytale Modding API (via `hytale-mod` plugin)

## Development Notes

- Java 25 toolchain required
- Asset sync runs automatically after server stops
- Manifest template uses Gradle property expansion
- All systems follow ECS pattern with explicit component registration
- Characteristic system uses additive/multiplicative modifiers based on stat type
- Damage calculation inspired by Dofus mechanics with elemental resistances capped at 50%