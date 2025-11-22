package com.thevoxelbox.yause.internal.orig.ftbquests.quest;

import java.util.Collections;
import java.util.List;

/** Very small QuestFile stub used by menu code when the full mod isn't present. */
public class QuestFile {
    public List<Chapter> getVisibleChapters(QuestData data, boolean excludeEmpty) {
        return Collections.emptyList();
    }
}
