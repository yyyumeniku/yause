package com.thevoxelbox.yause.internal.orig.ftbquests.client;

import com.thevoxelbox.yause.internal.orig.ftbquests.quest.Chapter;
import com.thevoxelbox.yause.internal.orig.ftbquests.quest.QuestData;

import java.util.List;

/**
 * Minimal stub of the real ClientQuestFile found in FTB-Quests 1.12.
 * We add this to the project to have a small, safe subset of client classes used by
 * the pause menu integration when the real mod is not available at runtime.
 *
 * NOTE: This is intentionally tiny — only the pieces we rely on.
 */
public class ClientQuestFile extends com.thevoxelbox.yause.internal.orig.ftbquests.quest.QuestFile {
    // Real mod uses "ClientQuestFile.INSTANCE" — keep same name here (stub)
    public static ClientQuestFile INSTANCE = null;

    /** Per-player/team data for the client. */
    public Object self = null; // loose type: our menu code uses reflection

    /**
     * Return visible chapters given player quest data. The real mod has more behavior;
     * our stub simply returns an empty list so menu code can still run when the real
     * mod isn't present. The menu will detect empty/absent results and show "no active".
     */
    public java.util.List<Chapter> getVisibleChapters(com.thevoxelbox.yause.internal.orig.ftbquests.quest.QuestData data, boolean excludeEmpty) {
        return java.util.Collections.emptyList();
    }
}
