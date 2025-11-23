package com.theyausebox.yause.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

/**
 * Forge GuiConfig-based config screen for YauseMenu.
 * This uses the existing Configuration instance exposed by YauseMenuConfig
 * and builds the config entries for the "menu" category.
 */
public class GuiYauseMenuConfig extends GuiConfig {

    public GuiYauseMenuConfig(GuiScreen parent) {
        super(parent,
                (List<IConfigElement>) new ConfigElement(YauseMenuConfig.getRawConfig().getCategory("menu")).getChildElements(),
                com.theyausebox.yause.YauseMenu.MODID,
                "menu",
                false,
                false,
                GuiConfig.getAbridgedConfigPath(YauseMenuConfig.getRawConfig().toString()),
                I18n.format("option.screen.title")
        );
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Save and reload configuration so runtime values reflect the changes
        try {
            com.theyausebox.yause.config.YauseMenuConfig.save();
            com.theyausebox.yause.config.YauseMenuConfig.reload();
            com.theyausebox.yause.YauseMenu.LOGGER.info("YauseMenu configuration reloaded from GuiConfig");
        } catch (Throwable t) {
            com.theyausebox.yause.YauseMenu.LOGGER.error("Failed to reload YauseMenu configuration: {}", t.getMessage());
        }
    }

}