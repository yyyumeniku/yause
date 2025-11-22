# Yause - Forge 1.12.2 Port

A complete port of the Yause (formerly VoxelMenu) mod from LiteLoader 1.7.10 to Forge 1.12.2. This project focuses on the in-game / pause menu UX and integrations (FTBU / FTBQuests) and no longer includes a custom main-menu build.

## Overview

Yause provides an improved pause menu with a clean layout and optional integrations with popular mods such as FTB Utilities and FTB Quests.

## Credits

- **Port author / maintainer**: yumeniku
- **Original Author**: Mumfrey (original VoxelMenu)

## Highlights

- Improved pause menu and button layout
- Optional playtime display (FTBU leaderboard preferred with vanilla fallback)
- Optional, reflection-based FTB Quests hints

## File structure (developer view)

```
src/main/java/com/thevoxelbox/yause/
  - core, config, event handlers, and controls for pause menu enhancements
src/main/resources/assets/yause/
  - lang, textures, and resources for the pause menu UI
```

## Configuration

The mod writes a developer configuration at `.minecraft/config/yause.cfg` during dev runs. Options include animation durations, playtime toggle, and quest hints.

## Development

Build and run the client with the wrapper (Java 17 recommended in this workspace):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew runClient --console=plain
```

## Notes

This repository has been rebranded from VoxelMenu → Yause and the primary maintainer is now listed as `yumeniku`.
