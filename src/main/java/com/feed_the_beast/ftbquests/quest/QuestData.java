package com.theyausebox.yause.internal.orig.ftbquests.quest;

import java.util.List;

/**
 * Minimal stub of FTB-Quests QuestData used by YauseMenu's lightweight integration.
 * This provides the minimal shape required for menu reflection to call methods such as
 * getDisplayName() and be passed around to chapter progress methods.
 */
public abstract class QuestData {
    public abstract short getTeamUID();
    public abstract String getTeamID();
    public abstract java.util.List<? extends net.minecraft.entity.player.EntityPlayer> getOnlineMembers();
    public abstract String getDisplayName();
    public abstract QuestFile getFile();
}
