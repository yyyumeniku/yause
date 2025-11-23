package com.theyausebox.yause;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.util.List;

public abstract class ForgeHandler {
    private static boolean forgeDetected = true; // Always true in Forge environment
    private static List<String> fmlBrandings;

    public static void init(String minecraftVersion) {
        // In 1.12.2, we're always running in Forge
        forgeDetected = true;
        try {
            // Retrieve brandings from FML and then clear them so we do not render MCP/FML brandings in the UI
            fmlBrandings = FMLCommonHandler.instance().getBrandings(true);
            // Clear brandings so they are not shown by default in our custom main menu build
            fmlBrandings = java.util.Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            fmlBrandings = java.util.Collections.emptyList();
        }
    }

    public static boolean isForgeDetected() {
        return forgeDetected;
    }

    public static boolean openModsList(Minecraft mc, GuiScreen parentScreen) {
        try {
            // In 1.12.2, use Forge's mod list GUI
            mc.displayGuiScreen(new net.minecraftforge.fml.client.GuiModList(parentScreen));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getBrandings() {
        return fmlBrandings;
    }

    public static boolean connectToServer(GuiScreen parentScreen, Minecraft mc, ServerData serverData) {
        try {
            // In 1.12.2, use FML's server connection
            net.minecraftforge.fml.client.FMLClientHandler.instance().setupServerList();
            net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServer(parentScreen, serverData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
