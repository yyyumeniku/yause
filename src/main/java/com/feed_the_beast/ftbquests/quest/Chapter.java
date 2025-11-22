package com.thevoxelbox.yause.internal.orig.ftbquests.quest;

/**
 * Minimal stub of FTB-Quests Chapter used by menu integration.
 * Only the small surface we call via reflection is provided here.
 */
public class Chapter {
    public String title = "";

    public String getTitle() {
        return title == null || title.isEmpty() ? "<unnamed>" : title;
    }

    /** Returns a percent [0..100] progress for this chapter for the given QuestData. */
    public int getRelativeProgressFromChildren(QuestData data) {
        return 0; // stub: default to 0
    }
}
