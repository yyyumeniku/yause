package com.theyausebox.yause.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Client -> Server: informs the server whether the client pause menu is open.
 */
public class PauseStateMessage implements IMessage {
    private boolean paused;

    public PauseStateMessage() {}

    public PauseStateMessage(boolean paused) { this.paused = paused; }

    public boolean isPaused() { return paused; }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(paused);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.paused = buf.readBoolean();
    }
}
