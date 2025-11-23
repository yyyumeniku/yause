package com.theyausebox.yause.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Server -> Client: contains the player's stored play ticks (long).
 */
public class ResponsePlaytimeMessage implements IMessage {
    private long playTicks;

    public ResponsePlaytimeMessage() {}

    public ResponsePlaytimeMessage(long playTicks) {
        this.playTicks = playTicks;
    }

    public long getPlayTicks() {
        return playTicks;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(playTicks);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playTicks = buf.readLong();
    }
}
