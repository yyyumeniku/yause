package com.theyausebox.yause.internal.ftbquests.quest;

import java.util.List;

public abstract class QuestData {
    public abstract short getTeamUID();
    public abstract String getTeamID();
    public abstract java.util.List<? extends net.minecraft.entity.player.EntityPlayer> getOnlineMembers();
    public abstract String getDisplayName();
    public abstract QuestFile getFile();
}
