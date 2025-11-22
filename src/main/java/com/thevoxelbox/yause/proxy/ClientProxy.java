package com.thevoxelbox.yause.proxy;

import com.thevoxelbox.yause.VoxelMenu;
import com.thevoxelbox.yause.config.VoxelMenuConfig;
import com.thevoxelbox.yause.event.GuiEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        
        // Initialize configuration
        VoxelMenuConfig.init(event.getSuggestedConfigurationFile());
        
        VoxelMenu.LOGGER.info("Yause client pre-initialization complete");
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
        
        VoxelMenu.LOGGER.info("Yause client initialization complete");
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        
        VoxelMenu.LOGGER.info("Yause client post-initialization complete");
    }
}
