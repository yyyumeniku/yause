package com.theyausebox.yause.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side handler that tracks playtime in ticks in player's entity data.
 */
public class PlayerPlaytimeTracker {
    public static final String PLAYTIME_KEY = "yause_playtime_ticks";

    private static final ConcurrentHashMap<UUID, Boolean> PAUSED = new ConcurrentHashMap<>();

    public PlayerPlaytimeTracker() {}

    public static void setPlayerPaused(UUID id, boolean paused) {
        if (id == null) return;
        if (paused) {
            PAUSED.put(id, Boolean.TRUE);
        } else {
            PAUSED.remove(id);
        }
    }

    public static boolean isPlayerPaused(UUID id) {
        if (id == null) return false;
        Boolean v = PAUSED.get(id);
        return v != null && v.booleanValue();
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.player == null) return;
        if (event.player.world.isRemote) return; // server side only
        if (event.phase != PlayerTickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        try {
            // Store playtime inside the player's persisted data so it survives restarts
            NBTTagCompound root = player.getEntityData();
            // perform safe migration: older versions may have stored playtime at the
            // entity root under PLAYTIME_KEY. Move that into PlayerPersisted so data
            // survives restarts and follows vanilla conventions.
            NBTTagCompound persisted = root.getCompoundTag("PlayerPersisted");
            if (root.hasKey(PLAYTIME_KEY) && !persisted.hasKey(PLAYTIME_KEY)) {
                try {
                    long old = root.getLong(PLAYTIME_KEY);
                    persisted.setLong(PLAYTIME_KEY, old);
                    root.removeTag(PLAYTIME_KEY);
                } catch (Throwable ignore) { }
            }
            // skip increment when the client reported they are paused
            if (PlayerPlaytimeTracker.isPlayerPaused(player.getUniqueID())) return;

            long value = 0L;
            if (persisted.hasKey(PLAYTIME_KEY)) {
                value = persisted.getLong(PLAYTIME_KEY);
            }
            value++;
            persisted.setLong(PLAYTIME_KEY, value);
            root.setTag("PlayerPersisted", persisted);
        } catch (Throwable ignore) { }
    }

    // Copy stored playtime across respawn/clone events so the value persists
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntityPlayer() == null || event.getOriginal() == null) return;
        try {
            NBTTagCompound oldRoot = event.getOriginal().getEntityData();
            NBTTagCompound newRoot = event.getEntityPlayer().getEntityData();
            // also fallback to copying from old top-level key for migration compatibility
            NBTTagCompound oldPersisted = oldRoot.getCompoundTag("PlayerPersisted");
            boolean copied = false;
            if (oldPersisted != null && oldPersisted.hasKey(PLAYTIME_KEY)) {
                NBTTagCompound newPersisted = newRoot.getCompoundTag("PlayerPersisted");
                newPersisted.setLong(PLAYTIME_KEY, oldPersisted.getLong(PLAYTIME_KEY));
                newRoot.setTag("PlayerPersisted", newPersisted);
                copied = true;
            }
            // copy pause flag if present for the player's UUID
            try {
                java.util.UUID oldId = event.getOriginal().getUniqueID();
                if (oldId != null) {
                    boolean p = PAUSED.getOrDefault(oldId, Boolean.FALSE);
                    if (p) {
                        PlayerPlaytimeTracker.setPlayerPaused(event.getEntityPlayer().getUniqueID(), true);
                    }
                }
            } catch (Throwable ignore) { }
            if (!copied && oldRoot.hasKey(PLAYTIME_KEY)) {
                try {
                    long oldVal = oldRoot.getLong(PLAYTIME_KEY);
                    NBTTagCompound newPersisted = newRoot.getCompoundTag("PlayerPersisted");
                    newPersisted.setLong(PLAYTIME_KEY, oldVal);
                    newRoot.setTag("PlayerPersisted", newPersisted);
                } catch (Throwable ignore) { }
            }
        } catch (Throwable ignore) { }
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new PlayerPlaytimeTracker());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        if (event.player != null) {
            PAUSED.remove(event.player.getUniqueID());
        }
    }
}
