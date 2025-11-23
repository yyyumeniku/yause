package com.theyausebox.yause.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Client -> Server message asking for the player's stored playtime on the server.
 */
public class RequestPlaytimeMessage implements IMessage {

    public RequestPlaytimeMessage() {}

    @Override
    public void toBytes(ByteBuf buf) {
        // no payload
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // no payload
    }
}
