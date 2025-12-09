package com.theyausebox.yause.proxy;

import com.theyausebox.yause.network.NetworkHandler;
import com.theyausebox.yause.server.PlayerPlaytimeTracker;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        NetworkHandler.init("yause");
    }

    public void init(FMLInitializationEvent event) {
        PlayerPlaytimeTracker.register();
    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}
