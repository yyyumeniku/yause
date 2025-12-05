package com.theyausebox.yause;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.util.List;

public abstract class ForgeHandler {
    private static boolean forgeDetected = true;
    private static List<String> fmlBrandings;

    public static void init(String minecraftVersion) {

        forgeDetected = true;
        try {

            fmlBrandings = FMLCommonHandler.instance().getBrandings(true);

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

            net.minecraftforge.fml.client.FMLClientHandler.instance().setupServerList();
            net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServer(parentScreen, serverData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
