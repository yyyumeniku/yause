package com.theyausebox.yause.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestPlaytimeMessageHandler implements IMessageHandler<RequestPlaytimeMessage, IMessage> {

    @Override
    public IMessage onMessage(RequestPlaytimeMessage message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        // schedule on main thread
        player.getServerWorld().addScheduledTask(() -> {
            NBTTagCompound root = player.getEntityData();
            NBTTagCompound persisted = root.getCompoundTag("PlayerPersisted");
            long ticks = 0L;
            // Prefer persisted value. If not present, fall back to older top-level key
            // and migrate it into PlayerPersisted for future reads.
            if (persisted.hasKey("yause_playtime_ticks")) {
                try {
                    ticks = persisted.getLong("yause_playtime_ticks");
                } catch (Throwable ignore) { ticks = 0L; }
            } else if (root.hasKey("yause_playtime_ticks")) {
                try {
                    ticks = root.getLong("yause_playtime_ticks");
                    // migrate into persisted compound so it survives restarts
                    persisted.setLong("yause_playtime_ticks", ticks);
                    root.setTag("PlayerPersisted", persisted);
                    root.removeTag("yause_playtime_ticks");
                } catch (Throwable ignore) { ticks = 0L; }
            }

            NetworkHandler.get().sendTo(new ResponsePlaytimeMessage(ticks), player);
        });

        return null; // no response queued here (we send async above)
    }
}
