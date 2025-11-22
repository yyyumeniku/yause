# VoxelMenu (Legacy) - See Yause

This repository has been rebranded and updated. The project previously known as "VoxelMenu" is now published as **Yause**.

Please see YAUSE_PORT_README.md for the current port and maintenance information.

## Overview

VoxelMenu now focuses on enhancing the in-game / pause menu with improved button layout and optional integrations (FTBU / FTBQuests).
 - **Restyled Buttons** - Custom button designs and layouts used by the pause menu
 - **Custom Buttons** - Lightweight controls for modular in-menu buttons
 - **Server Integration** - Right-click favorite server to edit

## Original Credits (Retained)

- **Original Author**: Mumfrey (original VoxelMenu)
- **Port**: Ported to Forge 1.12.2 (now maintained under Yause)

## Features Ported

### ✅ Fully Ported
 - Custom button controls (GuiButtonMainMenu, GuiButtonPanel)
 - Screen transitions and menu-music systems were removed along with the main-menu UI in this port; this project now focuses on pause/in-game menu features.
- All textures and assets
- Configuration system using Forge's config
- Forge integration (mod list, server connectivity)

### 📝 Simplified/Modified
 - Music playback support removed along with main-menu replacement
- Panorama rendering - uses native Forge/Minecraft systems
- LiteLoader specific features removed (replaced with Forge equivalents)

### ⚠️ Not Yet Ported
- Dialog boxes (GuiDialogBoxFavouriteServer, GuiDialogBoxConfirmDelete, GuiDialogBoxResourcePackGuiOptions)
- Resource pack GUI options
- Server data fetching thread
- Some advanced VoxelCommon library features

## File Structure

```
forge-voxelmenu/
├── src/main/java/com/thevoxelbox/voxelmenu/
│   ├── VoxelMenuModCore.java - Core functionality
│   ├── (custom main menu removed) - main menu replacement was intentionally removed; this port focuses on the in-game/pause menu and controls
### ✅ Fully Ported
- In-game / pause menu features and controls (main menu replacement has been removed from this port)
│   ├── proxy/
│   ├── event/
│   │   └── GuiEventHandler.java - GUI event handling
│   ├── controls/ - Custom button implementations used by the pause menu
│   │   ├── GuiButtonMainMenu.java
│   │   └── GuiButtonPanel.java
└── src/main/resources/
    ├── mcmod.info - Mod metadata
    └── assets/
        ├── voxelmenu/
      │   ├── sounds/ - (main-menu music removed)
      │   ├── sounds.json - (unused)
        │   ├── textures/ - Custom textures
        │   └── lang/ - Localization
        ├── voxelcommon/
        │   └── textures/ - Common GUI textures
        └── minecraft/
            └── textures/ - Minecraft texture overrides
```

## Installation

1. Ensure you have **Minecraft 1.12.2** with **Forge 14.23.5.2847** or later
2. Download `voxelmenu-1.0.0.jar` from the build
3. Place the jar file in your `.minecraft/mods` folder
4. Launch Minecraft 1.12.2 with Forge

## Configuration

The mod creates a configuration file at `.minecraft/config/voxelmenu.cfg` with the following options:

```
general {
   (custom main menu replacement removed — this port focuses on the in-game/pause menu)
    enableMenuMusic=true
}

menu {
   disableTransitions=false
    transitionType=fade
    transitionDuration=500
}

server {
    favouriteServerName=
    favouriteServerIP=
}
```

## Development

### Building from Source

Requirements:
- JDK 21 (Eclipse Temurin recommended)
- Gradle 8.12 (included via wrapper)

```bash
cd forge-voxelmenu
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # macOS
./gradlew setupDecompWorkspace
./gradlew build
```

The compiled mod will be in `build/libs/voxelmenu-1.0.0.jar`

### Running in Development

```bash
./gradlew runClient
```

## Technical Details

### Key Changes from 1.7.10 to 1.12.2

1. **Class Name Mappings**:
   - `bao` → `Minecraft`
   - `bdw` → `GuiScreen`
   - `bee` → `GuiMainMenu`
   - `bmh` → `GuiButton`
   - `bqx` → `ResourceLocation`
   - And many more...

2. **Rendering System**:
   - Converted from old Tessellator to BufferBuilder with VertexFormats
   - Updated OpenGL calls for 1.12.2

3. **Event System**:
   - LiteLoader hooks → Forge event bus (`GuiOpenEvent`)
   - Direct menu replacement via event interception

4. **Configuration**:
   - LiteLoader config → Forge Configuration system

5. **Sound System**:
   - LiteLoader music manager → Minecraft SoundEvent system
   - Updated sounds.json format for 1.12.2

## Known Issues

1. Screen transition / FBO features were removed with the custom main-menu
2. Dialog boxes not yet ported (planned for future update)
3. Server data fetching simplified
4. Some VoxelCommon library features not fully implemented

## Future Improvements

- [ ] Port dialog boxes for server favorites
- [ ] Implement in-game menu customization
- [ ] Add more transition effects
- [ ] Improve FBO system for better transition quality
- [ ] Add more configuration options
- [ ] Support for custom music pack loading

## License

Original mod was part of the VoxelModPack. This port maintains compatibility with the original mod's structure while adapting it for Forge 1.12.2.

## Version History

### 1.0.0 (Initial Port)
- Ported from LiteLoader 1.7.10 to Forge 1.12.2
- Combined VoxelMenu and VoxelMenuMusic into single mod
- Implemented Forge event-based menu replacement
   - Removed the custom main menu replacement; this port concentrates on the pause menu UX and integrations
- Included all 5 music tracks
- Created Forge configuration system

## Support

For issues or questions about this port, please note this is a community port of the original VoxelMenu mod.

---

**Built with** ❤️ **using CleanroomMC's ForgeDevEnv template**
