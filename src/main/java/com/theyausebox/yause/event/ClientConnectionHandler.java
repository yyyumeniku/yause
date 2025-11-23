package com.theyausebox.yause.event;

import com.theyausebox.yause.client.ClientPlaytimeCache;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client-side network connection events. We clear the playtime cache on connect/disconnect
 * so stale values from previous worlds don't leak into servers that do not have the mod.
 */
@SideOnly(Side.CLIENT)
public class ClientConnectionHandler {

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ClientPlaytimeCache.clear();
    }

    @SubscribeEvent
    public void onClientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ClientPlaytimeCache.clear();
    }
}
