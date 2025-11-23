package com.theyausebox.yause.network;

import com.theyausebox.yause.server.PlayerPlaytimeTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PauseStateMessageHandler implements IMessageHandler<PauseStateMessage, IMessage> {

    @Override
    public IMessage onMessage(PauseStateMessage message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        // schedule on server main thread
        player.getServerWorld().addScheduledTask(() -> {
            PlayerPlaytimeTracker.setPlayerPaused(player.getUniqueID(), message.isPaused());
        });
        return null;
    }
}
