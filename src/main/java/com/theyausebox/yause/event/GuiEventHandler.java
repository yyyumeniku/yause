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
        // Always replace the in-game (pause/escape) menu so pause features like playtime
        // are available regardless of whether the custom main menu is enabled.
        if (event.getGui() instanceof GuiIngameMenu) {
            event.setGui(new GuiIngameMenuYauseBox());
            return;
        }

        // We only replace the in-game (pause/escape) menu. The mod no longer replaces
        // the vanilla main menu — keep the main menu untouched and only swap the
        // in-game menu so pause-time features stay intact.
    }
}
