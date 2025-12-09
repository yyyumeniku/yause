package com.theyausebox.yause.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class NetworkHandler {
    private NetworkHandler() {}

    private static SimpleNetworkWrapper INSTANCE;

    public static void init(String modId) {
        if (INSTANCE != null) return;
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(modId);

        final int ID_REQUEST_PLAYTIME = 0;
        final int ID_RESPONSE_PLAYTIME = 1;
        final int ID_PAUSE_STATE = 2;
        INSTANCE.registerMessage(RequestPlaytimeMessageHandler.class, RequestPlaytimeMessage.class, ID_REQUEST_PLAYTIME, Side.SERVER);
        INSTANCE.registerMessage(PauseStateMessageHandler.class, PauseStateMessage.class, ID_PAUSE_STATE, Side.SERVER);

    }

    public static void registerClientHandlers() {
        if (INSTANCE == null) return;
        final int ID_RESPONSE_PLAYTIME = 1;
        INSTANCE.registerMessage(ResponsePlaytimeMessageHandler.class, ResponsePlaytimeMessage.class, ID_RESPONSE_PLAYTIME, Side.CLIENT);
    }

    public static SimpleNetworkWrapper get() {
        return INSTANCE;
    }
}
