package com.theyausebox.yause.internal.ftbquests.quest;

import java.util.Collections;
import java.util.List;

/** Minimal internal QuestFile stub used when the mod isn't present. */
public class QuestFile {
    public List<Chapter> getVisibleChapters(QuestData data, boolean excludeEmpty) {
        return Collections.emptyList();
    }
}
