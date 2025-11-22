package com.thevoxelbox.yause;

import com.thevoxelbox.yause.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Loader;

@Mod(
    modid = VoxelMenu.MODID,
    name = VoxelMenu.NAME,
    version = VoxelMenu.VERSION,
    clientSideOnly = true,
    // Make FTBU optional so the mod can still start while we show a clear warning if FTBU is missing.
    dependencies = "after:ftbutilities",
    guiFactory = "com.thevoxelbox.yause.config.VoxelMenuGuiFactory"
)
public class VoxelMenu {
    public static final String MODID = "yause";
    public static final String NAME = "Yause";
    public static final String VERSION = "1.0.0";
    
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    // Cached mod presence for optional integrations
    public static boolean ftbQuestsInstalled = false;
    public static boolean ftbUtilitiesInstalled = false;
    
    @Mod.Instance(MODID)
    public static VoxelMenu instance;
    
    @SidedProxy(
        clientSide = "com.thevoxelbox.yause.proxy.ClientProxy",
        serverSide = "com.thevoxelbox.yause.proxy.CommonProxy"
    )
    public static CommonProxy proxy;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Yause PreInit");
        proxy.preInit(event);
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Yause Init");
        proxy.init(event);
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("Yause PostInit");
        // One-time detections of FTB mods at mod load time
        ftbQuestsInstalled = Loader.isModLoaded("ftbquests");
        ftbUtilitiesInstalled = Loader.isModLoaded("ftbutilities");
        // One-time FTB detection: print once at mod load
        LOGGER.info("FTB integration - ftbquests: {}, ftbutilities: {}", ftbQuestsInstalled, ftbUtilitiesInstalled);

        // Clear, actionable message if FTBU is not present (we made FTBU optional). This makes it explicit
        // in the logs that FTBU features (playtime) will be disabled and why the UI may not show playtime.
            if (!ftbUtilitiesInstalled) {
                // FTBU is optional — but we require FTBU for the playtime feature. When missing, playtime will be disabled.
                LOGGER.info("FTB Utilities (ftbutilities) not found — FTBU-only playtime display will be disabled.");
        }
        proxy.postInit(event);
    }
}
