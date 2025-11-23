package com.theyausebox.yause.internal.ftbquests.client;

import com.theyausebox.yause.internal.ftbquests.quest.QuestData;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collections;
import java.util.List;

/** Minimal client-side QuestData stub used by the menu when the real mod is absent. */
public class ClientQuestData extends QuestData {
    private final short teamUID = 0;
    private final String teamID = "";

    @Override
    public short getTeamUID() { return teamUID; }

    @Override
    public String getTeamID() { return teamID; }

    @Override
    public List<? extends EntityPlayer> getOnlineMembers() { return Collections.emptyList(); }

    @Override
    public String getDisplayName() { return "Player"; }

    @Override
    public com.theyausebox.yause.internal.ftbquests.quest.QuestFile getFile() { return null; }
}
