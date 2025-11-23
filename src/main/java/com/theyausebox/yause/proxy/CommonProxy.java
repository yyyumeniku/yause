package com.theyausebox.yause.proxy;

import com.theyausebox.yause.network.NetworkHandler;
import com.theyausebox.yause.server.PlayerPlaytimeTracker;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    
    public void preInit(FMLPreInitializationEvent event) {
        // Common pre-initialization code
        // Initialize network channel for playtime messages
        NetworkHandler.init("yause");
    }
    
    public void init(FMLInitializationEvent event) {
        // Common initialization code
        // Register server-side playtime tracking (safe on both sides, handler ignores client-side ticks)
        PlayerPlaytimeTracker.register();
    }
    
    public void postInit(FMLPostInitializationEvent event) {
        // Common post-initialization code
    }
}
