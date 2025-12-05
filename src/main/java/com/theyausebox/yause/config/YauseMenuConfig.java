package com.theyausebox.yause.config;

import com.theyausebox.yause.YauseMenu;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class YauseMenuConfig {

    private static Configuration config;

    public static Configuration getRawConfig() {
        return config;
    }

    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_MENU = "menu";
    private static final String CATEGORY_SERVER = "server";

    public static boolean disableTransitions;

    public static boolean enableQuests;

    public static boolean showPlaytime;

    public static int menuOffsetX;
    public static int openAnimationMs;
    public static int closeAnimationMs;

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

            disableTransitions = config.getBoolean(
                "disableTransitions",
                CATEGORY_MENU,
                false,
                "Disable screen transitions (when true, animations are turned off)"
            );

            menuOffsetX = config.getInt(
                "menuOffsetX",
                CATEGORY_MENU,
                20,
                0,
                200,
                "Horizontal offset of the YauseMenu panel"
            );

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

            enableQuests = config.getBoolean(
                "enableQuests",
                CATEGORY_MENU,
                true,
                "Enable FTB-Quests hints in the pause menu when FTB-Quests is present."
            );

            showPlaytime = config.getBoolean(
                "showPlaytime",
                CATEGORY_MENU,
                true,
                "Show vanilla playtime in the pause menu (client-side; updates in real-time)."
            );

        } catch (Exception e) {
            YauseMenu.LOGGER.error("Error loading configuration", e);
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

    public static void reload() {
        if (config != null) {
            loadConfiguration();
        }
    }
}
