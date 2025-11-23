package com.theyausebox.yause.network;

import com.theyausebox.yause.client.ClientPlaytimeCache;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ResponsePlaytimeMessageHandler implements IMessageHandler<ResponsePlaytimeMessage, IMessage> {

    @Override
    public IMessage onMessage(ResponsePlaytimeMessage message, MessageContext ctx) {
        // execute on client main thread
        Minecraft.getMinecraft().addScheduledTask(() -> {
            long now = Minecraft.getSystemTime();
            ClientPlaytimeCache.setPlayTicks(message.getPlayTicks(), now);
        });
        return null;
    }
}
