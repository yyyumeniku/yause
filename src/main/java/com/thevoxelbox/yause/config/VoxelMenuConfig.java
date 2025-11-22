package com.thevoxelbox.yause.config;

import com.thevoxelbox.yause.VoxelMenu;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class VoxelMenuConfig {
    
    private static Configuration config;

    // Expose the raw Configuration to allow integration with Forge's GuiConfig
    public static Configuration getRawConfig() {
        return config;
    }
    
    // Configuration categories
    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_MENU = "menu";
    private static final String CATEGORY_SERVER = "server";
    
    // Configuration options
    // When true, disable screen transition animations
    public static boolean disableTransitions;
    // transitionType removed — screen transitions were removed with main menu
    // Toggle to display playtime in the pause menu
    public static boolean showPlaytime;
    // New flags: enableQuests controls whether FTB-Quests hints are shown in the pause
    // menu. Playtime is governed by showPlaytime and requires FTBU at runtime.
    public static boolean enableQuests;
    // Animation/placement configuration
    public static int menuOffsetX;
    public static int openAnimationMs;
    public static int closeAnimationMs;
    // Hover highlight animation speed (alpha falloff rate)
    public static double hoverAlphaFalloffRate;
    
    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            loadConfiguration();
        }
    }
    
    private static void loadConfiguration() {
        try {
            config.load();
            
            // General settings
            // The custom main menu replacement is always enabled — the separate toggle
            // has been removed to keep behaviour consistent with the pause-menu features.
            
            disableTransitions = config.getBoolean(
                "disableTransitions",
                CATEGORY_MENU,
                false,
                "Disable screen transitions (when true, animations are turned off)"
            );
            
            
            // Screen transitions were removed when the main menu was deleted.

            showPlaytime = config.getBoolean(
                "showPlaytime",
                CATEGORY_MENU,
                true,
                "Show player playtime in the pause menu"
            );
            
            menuOffsetX = config.getInt(
                "menuOffsetX",
                CATEGORY_MENU,
                20,
                0,
                200,
                "Horizontal offset of the VoxelMenu panel"
            );

            // Vertical offset removed from the configuration. Use a fixed default
            // vertical position in the UI instead. (menuOffsetY was removed.)

            openAnimationMs = config.getInt(
                "openAnimationMs",
                CATEGORY_MENU,
                250,
                10,
                5000,
                "Open animation duration in milliseconds"
            );

            closeAnimationMs = config.getInt(
                "closeAnimationMs",
                CATEGORY_MENU,
                250,
                10,
                5000,
                "Close animation duration in milliseconds"
            );

            hoverAlphaFalloffRate = config.getFloat(
                "hoverAlphaFalloffRate",
                CATEGORY_MENU,
                0.12f,
                0.01f,
                1.0f,
                "Alpha falloff rate for hover highlight animations"
            );


            // New flags: these control the *features* in the menu. When false, the corresponding
            // functionality will be disabled (not merely hidden). Default both to true.
            enableQuests = config.getBoolean(
                "enableQuests",
                CATEGORY_MENU,
                true,
                "Enable FTB-Quests hints in the pause menu when FTB-Quests is present."
            );
            
        } catch (Exception e) {
            VoxelMenu.LOGGER.error("Error loading configuration", e);
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    public static void save() {
        if (config != null && config.hasChanged()) {
            config.save();
        }
    }

    /**
     * Reload the configuration values into the static fields.
     * Call this after the Configuration has been changed (e.g. via a GuiConfig)
     * so runtime values reflect the new settings.
     */
    public static void reload() {
        if (config != null) {
            loadConfiguration();
        }
    }
}
