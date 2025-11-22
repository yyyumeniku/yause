package com.thevoxelbox.yause.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

/**
 * Forge GuiConfig-based config screen for VoxelMenu.
 * This uses the existing Configuration instance exposed by VoxelMenuConfig
 * and builds the config entries for the "menu" category.
 */
public class GuiVoxelMenuConfig extends GuiConfig {

    public GuiVoxelMenuConfig(GuiScreen parent) {
        super(parent,
                (List<IConfigElement>) new ConfigElement(VoxelMenuConfig.getRawConfig().getCategory("menu")).getChildElements(),
                com.thevoxelbox.yause.VoxelMenu.MODID,
                "menu",
                false,
                false,
                GuiConfig.getAbridgedConfigPath(VoxelMenuConfig.getRawConfig().toString()),
                I18n.format("option.screen.title")
        );
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Save and reload configuration so runtime values reflect the changes
        try {
            com.thevoxelbox.yause.config.VoxelMenuConfig.save();
            com.thevoxelbox.yause.config.VoxelMenuConfig.reload();
            com.thevoxelbox.yause.VoxelMenu.LOGGER.info("VoxelMenu configuration reloaded from GuiConfig");
        } catch (Throwable t) {
            com.thevoxelbox.yause.VoxelMenu.LOGGER.error("Failed to reload VoxelMenu configuration: {}", t.getMessage());
        }
    }

}