package com.theyausebox.yause.proxy;

import com.theyausebox.yause.YauseMenu;
import com.theyausebox.yause.config.YauseMenuConfig;
import com.theyausebox.yause.event.GuiEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        
        // Initialize configuration
        YauseMenuConfig.init(event.getSuggestedConfigurationFile());
        
        YauseMenu.LOGGER.info("Yause client pre-initialization complete");
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
        
        YauseMenu.LOGGER.info("Yause client initialization complete");
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        // FTBU playtime listeners removed — playtime feature deleted per configuration
        
        YauseMenu.LOGGER.info("Yause client post-initialization complete");
    }
}
