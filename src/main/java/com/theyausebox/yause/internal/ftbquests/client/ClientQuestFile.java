package com.theyausebox.yause.internal.ftbquests.client;

import com.theyausebox.yause.internal.ftbquests.quest.Chapter;
import com.theyausebox.yause.internal.ftbquests.quest.QuestData;

import java.util.List;

/**
 * Internal minimal stub used only when the real FTB-Quests mod classes are not present.
 * Placed in a distinct package to avoid runtime collisions with the real mod.
 */
public class ClientQuestFile extends com.theyausebox.yause.internal.ftbquests.quest.QuestFile {
    public static ClientQuestFile INSTANCE = null;
    public Object self = null; // loose type — the menu uses reflection

    public List<Chapter> getVisibleChapters(QuestData data, boolean excludeEmpty) {
        return java.util.Collections.emptyList();
    }
}
