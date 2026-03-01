# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Welltale is a Hytale mod that implements an RPG/MMO system with characteristics, classes, spells, levels, mobs, and multi-character player profiles. Built with Java 25 using the Hytale Modding API and Gradle with Kotlin DSL.

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
- Passive stat systems inspired by Dofus:
  - **MoveSpeedSystem**: Agility increases movement speed (0.1% per point)
  - **StaminaCostReductionSystem**: Strength reduces stamina consumption (0.1% per point, capped at 70%)
  - **LifeRegenSystem**: Intelligence increases health regeneration (0.1% per point as % bonus to base regen)
  - **DropChanceSystem**: Chance increases drop rate (0.1% per point)
  - **Wisdom**: Increases XP gain (0.1% per point) - handled in GiveXPHandler

**Level System** (`fr.welltale.level`)
- XP calculation using exponential curve: `A * level^2.8` where A=35
- Levels 1-200 supported
- PlayerLevelComponent attached to entities via custom component type
- OnDeathSystem grants XP to killer
- XPTable handles all XP/level calculations

**Class System** (`fr.welltale.clazz`)
- Classes contain spell slugs and configuration
- Players select class via UI page when creating/choosing a character slot
- ClassSelectPage handles class selection before entering gameplay

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
- Multi-character flow: character slot selection + active runtime character cache
- Character UUID is required for per-character scoping; if missing on legacy data, regenerate and persist when character is selected
- Characteristics points system for stat allocation
- Event systems for player actions (blocks, items, chat)
- HUD updates are event-driven from gameplay handlers/pages (no per-tick HUD update system)

**Inventory System** (`fr.welltale.inventory`)
- Inventory is character-scoped (player UUID + character UUID)
- Vanilla inventory sections persisted per character: `hotbar`, `storage`, `armor`
- Custom inventory sections persisted per character: `loot`, `equipment`
- Persist only lightweight stored item data (`StoredItemStack` with `itemId` + `quantity`) in JSON
- Do not persist Hytale runtime `ItemStack` directly in player JSON (can serialize huge object graphs and cyclic references)

## File Structure

```
src/main/java/fr/welltale/
├── Welltale.java              # Main plugin entry point
├── characteristic/             # Stats and damage systems
│   ├── Characteristics.java   # Constants and stat management
│   └── system/                 # DamageSystem, MoveSpeedSystem
├── clazz/                      # Class system
│   ├── Class.java              # Class data model
│   └── page/                   # Class selection UI (ClassSelectPage)
├── level/                      # XP and leveling
│   ├── PlayerLevelComponent.java # ECS component
│   ├── XPTable.java           # XP calculations
│   ├── event/                  # GiveXPEvent, LevelUpEvent
│   ├── handler/               # Event handlers
│   └── system/                 # OnDeathSystem
├── mob/                        # Enemy mobs
│   ├── Mob.java               # Mob data model
│   ├── MobStatsComponent.java # ECS component
│   └── system/                 # Mob stat assignment
├── player/                     # Player management
│   ├── Player.java            # Player data model
│   ├── charactercache/         # Active runtime character cache
│   ├── event/                  # Player events
│   ├── page/                   # CharacterSelectPage
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
│   └── Pages/                  # Game pages (inventory, characteristics, class/character selection)
├── Server/                     # Server-side resources
│   ├── Audio/                  # Sound events
│   ├── Entity/                 # Entity stats and damage causes
│   └── Item/                   # Items and interactions
```

## JSON Data Storage

All game data (players, ranks, classes, mobs) is stored as JSON files in `./mods/welltale/`:
- `players.json` - Player data with UUID, rank, gems, friends, and `characters[]` (class, XP, stats, etc.)
- `ranks.json` - Rank configurations
- `classes.json` - Class definitions with spell slugs
- `mobs.json` - Mob stat configurations

**Important**: File loaders auto-create these files with example data if missing.

### Player JSON Item Format

- Character inventory/item fields in `players.json` must use lightweight objects:
  - `{ "itemId": "...", "quantity": <int> }`
- Keep unknown/engine-only item fields out of persisted player data.

### Player/Character Invariants

- `Player.characters` is a slot list (target size: 6 slots, indices `0..5`).
- Active runtime character is uniquely keyed by player UUID in `CharacterCacheRepository`.
- `characterUuid` is mandatory for character-scoped systems (inventory, class, progression).
- If legacy character data has no `characterUuid`, regenerate and persist before gameplay continues.

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
- Packet interceptor registration should be idempotent per player (guard duplicate handler installs).

### Runtime Lifecycle Rules

- On character enter gameplay, hydrate runtime cache from selected character slot and apply stats/inventory snapshot.
- On entity remove/leave, sync runtime state back to persisted character data (XP + inventory + custom inventory sections).
- Always clear per-player runtime guards/caches on leave (packet interceptor install guards, stamina tracking maps, etc.).

## UI Development

- Custom UI defined in `.ui` files using Hytale UI format
- UI builders in Java code (e.g., `PlayerHudBuilder`, `ClassSelectPage`)
- HUD components use custom textures from `Common/UI/Custom/`
- Prefer responsive layouts using `FlexWeight` + parent constraints over fixed widths/heights.
- Keep backgrounds constrained to parent groups (no visual overflow outside the frame).
- For rows of stats/items, use `LayoutMode: Left` + fixed key columns + `FlexWeight: 1` spacer/content so values align and adapt.
- Keep labels and terminology consistent in French across the same page.
- Reuse existing shared textures/assets when possible instead of duplicating paths/styles.

### UI Docs Reference

- Layout docs: `https://hytalemodding.dev/en/docs/official-documentation/custom-ui/layout`
- Type docs: `https://hytalemodding.dev/en/docs/official-documentation/custom-ui/type-documentation`

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
- Active character state is cached in memory via `CharacterCacheRepository` and synced back on player entity removal
- Repository read methods generally return immutable snapshots (`List.copyOf(...)`) to avoid accidental external mutations
- Clean per-player runtime caches/maps on leave/disconnect (example: stamina tracking maps, interceptor install guards)

## Known Limitations (Current)

- `ClassSelectPage` currently creates new characters by append (`add(...)`) rather than strict slot replacement.
- `InventoryPage` is intentionally large/monolithic for now and planned for later refactor.
- Most repositories/caches still use linear lookups; migrate hot paths to indexed maps for high concurrency targets.

## Performance Targets

- Current practical target: ~100 concurrent players with stable gameplay loops.
- Launch target: ~500 concurrent players after load testing/profiling and data-path indexing improvements.
- Before large-scale launch, validate join/leave churn, loot flow, UI updates, and persistence under stress.

## Manual Smoke Checklist

- Character flow: create/select character, relog, and verify class/stats/XP/inventory restoration.
- Inventory flow: transfer, drag/drop, collect-all, equipment slots, and loot-cap behavior.
- Combat flow: spell stamina usage, cooldown behavior, and damage/nameplate consistency.
- Runtime cleanup: repeated join/leave cycles without duplicate packet handlers or stale per-player caches.

## Code Style

- Prefer early returns (guard clauses) to reduce nesting.
- For simple guards, use inline returns when possible:
  - `if (!variable) return;`
  - `if (!variable) return false;`
- Avoid verbose block form for trivial guards unless needed for readability or multiple statements.
