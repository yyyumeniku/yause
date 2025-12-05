package com.theyausebox.yause.event;

import com.theyausebox.yause.YauseMenu;
import com.theyausebox.yause.config.YauseMenuConfig;
import com.theyausebox.yause.GuiIngameMenuYauseBox;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEventHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {

        if (event.getGui() instanceof GuiIngameMenu) {
            event.setGui(new GuiIngameMenuYauseBox());
            return;
        }

    }
}
