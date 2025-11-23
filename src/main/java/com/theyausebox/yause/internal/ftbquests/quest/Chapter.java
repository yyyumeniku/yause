package com.theyausebox.yause.internal.ftbquests.quest;

/**
 * Minimal internal Chapter stub used by the menu integration.
 */
public class Chapter {
    public String title = "";

    public String getTitle() { return title == null || title.isEmpty() ? "<unnamed>" : title; }

    public int getRelativeProgressFromChildren(QuestData data) { return 0; }
}
