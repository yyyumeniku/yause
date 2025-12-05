package com.theyausebox.yause.internal.ftbquests.client;

import com.theyausebox.yause.internal.ftbquests.quest.Chapter;
import com.theyausebox.yause.internal.ftbquests.quest.QuestData;

import java.util.List;

public class ClientQuestFile extends com.theyausebox.yause.internal.ftbquests.quest.QuestFile {
    public static ClientQuestFile INSTANCE = null;
    public Object self = null;

    public List<Chapter> getVisibleChapters(QuestData data, boolean excludeEmpty) {
        return java.util.Collections.emptyList();
    }
}
