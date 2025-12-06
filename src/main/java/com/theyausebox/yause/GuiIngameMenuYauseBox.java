package com.theyausebox.yause;

import com.theyausebox.yause.controls.GuiButtonPanel;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.resources.I18n;
import com.theyausebox.yause.config.YauseMenuConfig;

public class GuiIngameMenuYauseBox extends GuiIngameMenu {
    private GuiButtonPanel buttonPanelLeft;
    private int updateCounter = 0;
    private long openStartTimeMs = -1;
    private int openDurationMs = com.theyausebox.yause.config.YauseMenuConfig.openAnimationMs;
    public static float currentOpenProgress = 1.0f;

    private boolean isClosing = false;
    private long closeStartTimeMs = -1;
    private int closeDurationMs = com.theyausebox.yause.config.YauseMenuConfig.closeAnimationMs;

    private Long cachedVanillaPlayTicks = null;
    private long lastVanillaReadMs = 0L;

    private String cachedFTBText = null;
    private boolean cachedFTBHasActive = false;
    private static boolean ftbQuestsUnavailableLogged = false;
    private static boolean ftbQuestsMissingInstanceLogged = false;
    private static boolean ftbQuestsMissingSelfLogged = false;

    @Override
    public void initGui() {
        this.buttonList.clear();

            int marginLeft = com.theyausebox.yause.config.YauseMenuConfig.menuOffsetX;
            int marginTop = 10;
            int marginBottom = 0;
            int titleSpacing = 4;
            int buttonTopPadding = 0;
        int boxWidth = 200;
        int boxHeight = this.height - marginTop - marginBottom;

        int titleHeight = this.fontRenderer.FONT_HEIGHT;

            float scale = 2.0f;
            int titleDrawHeight = (int)(titleHeight * scale);
            int buttonBottomPadding = 4;

            int panelHeight = Math.max(48, boxHeight - titleDrawHeight - titleSpacing - buttonBottomPadding - buttonTopPadding);

            int panelTopDesired = marginTop + titleDrawHeight + titleSpacing + buttonTopPadding;

            int panelYOffset = Math.max(0, this.height - panelHeight - panelTopDesired);

        int panelWidth = boxWidth - 8;
            if (this.buttonPanelLeft == null || this.buttonPanelLeft.getPanelWidth() != panelWidth || this.buttonPanelLeft.getPanelHeight() != panelHeight || this.buttonPanelLeft.getYOffset() != panelYOffset) {

                this.buttonPanelLeft = new GuiButtonPanel(100, GuiButtonPanel.AnchorType.BottomLeft,
                marginLeft + 10, panelYOffset, panelWidth, panelHeight, 20, this.width, this.height, "left");
            this.initPanelButtons();
        } else {
            this.buttonPanelLeft.updatePosition(this.width, this.height);
        }

        this.buttonList.add(this.buttonPanelLeft);

        this.openStartTimeMs = net.minecraft.client.Minecraft.getSystemTime();

        this.openDurationMs = com.theyausebox.yause.config.YauseMenuConfig.openAnimationMs;
        this.closeDurationMs = com.theyausebox.yause.config.YauseMenuConfig.closeAnimationMs;

        this.cachedVanillaPlayTicks = null;
        this.lastVanillaReadMs = net.minecraft.client.Minecraft.getSystemTime();
        if (YauseMenuConfig.showPlaytime && this.mc != null && this.mc.player != null) {
            this.cachedVanillaPlayTicks = getVanillaPlayTicks();
        }

        if (YauseMenuConfig.enableQuests && com.theyausebox.yause.YauseMenu.ftbQuestsInstalled) {
            updateFTBCache();
        }
    }

    private void updateFTBCache() {
        this.cachedFTBText = null;
        this.cachedFTBHasActive = false;

        try {
            Class<?> clientQuestFileCls = null;

            try {
                clientQuestFileCls = Class.forName("com.feed_the_beast.ftbquests.client.ClientQuestFile");
            } catch (ClassNotFoundException ex) {
                try {
                    clientQuestFileCls = Class.forName("com.theyausebox.yause.internal.ftbquests.client.ClientQuestFile");
                } catch (ClassNotFoundException ex2) {
                    throw ex;
                }
            }
            java.lang.reflect.Field instanceField = clientQuestFileCls.getField("INSTANCE");
            Object instance = instanceField.get(null);
            java.lang.reflect.Field selfField = clientQuestFileCls.getField("self");
            if (instance == null) {
                if (!ftbQuestsMissingInstanceLogged) {
                    ftbQuestsMissingInstanceLogged = true;
                    com.theyausebox.yause.YauseMenu.LOGGER.debug("FTB Quests: ClientQuestFile.INSTANCE is null (no client instance available)");
                }
            }

            Object self = selfField.get(instance);
            if (self == null) {
                if (!ftbQuestsMissingSelfLogged) {
                    ftbQuestsMissingSelfLogged = true;
                    com.theyausebox.yause.YauseMenu.LOGGER.debug("FTB Quests: ClientQuestFile.self is null (no per-player quest data available)");
                }
                return;
            }

            try {
                Class<?> questDataCls = null;
                try {
                    questDataCls = Class.forName("com.feed_the_beast.ftbquests.quest.QuestData");
                } catch (ClassNotFoundException ex) {

                    questDataCls = Class.forName("com.theyausebox.yause.internal.ftbquests.quest.QuestData");
                }
                java.lang.reflect.Method visibleChaptersMethod = null;

                for (java.lang.reflect.Method m : clientQuestFileCls.getMethods()) {
                    if (m.getName().equals("getVisibleChapters") && m.getParameterCount() == 2) {
                        Class<?>[] pts = m.getParameterTypes();
                        if (pts[0].isAssignableFrom(questDataCls) || pts[0].getName().equals("com.feed_the_beast.ftbquests.quest.QuestData")) {
                            visibleChaptersMethod = m;
                            break;
                        }
                    }
                }

                if (visibleChaptersMethod != null) {
                    try {
                        Object chaptersObj = visibleChaptersMethod.invoke(instance, self, false);
                        if (chaptersObj instanceof java.util.List) {
                            java.util.List<?> chapters = (java.util.List<?>) chaptersObj;
                            if (!chapters.isEmpty()) {

                                Object activeChapter = null;
                                int activePercent = -1;
                                for (Object chap : chapters) {
                                    if (chap == null) continue;
                                    try {
                                        java.lang.reflect.Method prog = chap.getClass().getMethod("getRelativeProgressFromChildren", questDataCls);
                                        Object pval = prog.invoke(chap, self);
                                        int pct = pval instanceof Number ? ((Number)pval).intValue() : Integer.parseInt(pval.toString());
                                        if (pct > 0 && pct < 100) {
                                            activeChapter = chap;
                                            activePercent = pct;
                                            break;
                                        }
                                    } catch (Throwable ignore) { }
                                }

                                if (activeChapter != null) {
                                    String chapterTitle = null;
                                    try {
                                        java.lang.reflect.Method titleMethod = null;
                                        for (String nm : new String[]{"getTitle","getDisplayName","getAltTitle"}) {
                                            try { titleMethod = activeChapter.getClass().getMethod(nm); break; } catch (NoSuchMethodException ignored) {}
                                        }
                                        if (titleMethod != null) {
                                            Object tt = titleMethod.invoke(activeChapter);
                                            chapterTitle = tt == null ? null : tt.toString();
                                        }
                                    } catch (Throwable ignore) { }

                                    this.cachedFTBHasActive = true;
                                        chapterTitle = chapterTitle == null ? "<unnamed>" : chapterTitle;
                                        this.cachedFTBText = "Chapter: " + chapterTitle + " (" + activePercent + "%)";
                                    return;
                                }
                            }
                        }
                    } catch (Throwable ignore) {}
                }
            } catch (Throwable ignore) {}

            this.cachedFTBHasActive = true;
            this.cachedFTBText = "No active chapter";
            return;

        } catch (Throwable t) {
            if (!ftbQuestsUnavailableLogged) {
                ftbQuestsUnavailableLogged = true;
                String msg = t.getMessage() == null ? t.getClass().getName() : t.getMessage();
                com.theyausebox.yause.YauseMenu.LOGGER.warn("FTB Quests reflection failed while reading quests: {}", msg);
            }
        }
    }

    private Long getVanillaPlayTicks() {
        try {
            if (this.mc == null || this.mc.player == null) return null;
            net.minecraft.stats.StatBase stat = net.minecraft.stats.StatList.PLAY_ONE_MINUTE;
            if (stat == null) return null;

            try {
                Object statsManagerDirect = this.mc.player.getStatFileWriter();
                if (statsManagerDirect != null) {
                    try {
                        java.lang.reflect.Method readStat = statsManagerDirect.getClass().getMethod("readStat", net.minecraft.stats.StatBase.class);
                        Object val = readStat.invoke(statsManagerDirect, stat);
                        if (val instanceof Number) return ((Number) val).longValue();
                        if (val != null) return Long.parseLong(val.toString());
                    } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {

                    }
                }
            } catch (Throwable ignored) {

            }

            Object statsManager = null;
            try {

                java.lang.reflect.Field f = this.mc.player.getClass().getDeclaredField("statFileWriter");
                f.setAccessible(true);
                statsManager = f.get(this.mc.player);
            } catch (Throwable ignored2) { }

            if (statsManager == null) return null;

            if (statsManager != null) {
                try {
                    java.lang.reflect.Method readStat = statsManager.getClass().getMethod("readStat", net.minecraft.stats.StatBase.class);
                    Object val = readStat.invoke(statsManager, stat);
                    if (val instanceof Number) return ((Number) val).longValue();
                    if (val != null) return Long.parseLong(val.toString());
                } catch (NoSuchMethodException e) {

                    for (java.lang.reflect.Method m : statsManager.getClass().getMethods()) {
                        if (m.getParameterCount() == 1) {
                            Class<?> p = m.getParameterTypes()[0];
                            if (p == net.minecraft.stats.StatBase.class || p.getName().toLowerCase().contains("stat")) {
                                try {
                                    Object val = m.invoke(statsManager, stat);
                                    if (val instanceof Number) return ((Number) val).longValue();
                                    if (val != null) return Long.parseLong(val.toString());
                                } catch (Throwable ignored3) { }
                            }
                        }
                    }
                } catch (Throwable ignored) { }
            }
        } catch (Throwable ignored) { }
        return null;
    }

    private void initPanelButtons() {

        this.buttonPanelLeft.addButton(I18n.format("menu.returnToGame"));
        this.buttonPanelLeft.addButton(I18n.format("gui.advancements"));

        this.buttonPanelLeft.addButton(I18n.format("gui.stats"));
        this.buttonPanelLeft.addButton(I18n.format("menu.options"));

        this.buttonPanelLeft.addButton(I18n.format("fml.menu.mods"));

        if (this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic()) {
            this.buttonPanelLeft.addButton(I18n.format("menu.shareToLan"));
        }

        this.buttonPanelLeft.addButton(I18n.format(this.mc.isIntegratedServerRunning() ? "menu.returnToMenu" : "menu.disconnect"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {

        if (button.id == this.buttonPanelLeft.id) {
            button = this.buttonPanelLeft.getPressedButton();
        }

        String buttonText = button.displayString;

            if (buttonText.equals(I18n.format("menu.returnToGame"))) {

            if (com.theyausebox.yause.config.YauseMenuConfig.disableTransitions) {
                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();
                return;
            }
            if (!this.isClosing) {
                this.isClosing = true;
                this.closeStartTimeMs = net.minecraft.client.Minecraft.getSystemTime();
            }
        } else if (buttonText.equals(I18n.format("menu.options"))) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        } else if (buttonText.equals(I18n.format("fml.menu.mods"))) {

            boolean ok = com.theyausebox.yause.ForgeHandler.openModsList(this.mc, this);
            if (!ok) {
                com.theyausebox.yause.YauseMenu.LOGGER.warn("Failed to open mod list GUI via ForgeHandler");
            }
        } else if (buttonText.equals(I18n.format("gui.advancements"))) {
            this.mc.displayGuiScreen(new GuiScreenAdvancements(this.mc.player.connection.getAdvancementManager()));
        } else if (buttonText.equals(I18n.format("gui.stats"))) {
            try {

                this.mc.displayGuiScreen(new net.minecraft.client.gui.achievement.GuiStats(this, this.mc.player.getStatFileWriter()));
            } catch (Throwable t) {
                com.theyausebox.yause.YauseMenu.LOGGER.warn("Failed to open vanilla Stats GUI: {}", t.getMessage());
            }
        } else if (buttonText.equals(I18n.format("menu.shareToLan"))) {
            this.mc.displayGuiScreen(new GuiShareToLan(this));
        } else if (buttonText.equals(I18n.format("menu.returnToMenu")) || buttonText.equals(I18n.format("menu.disconnect"))) {
            boolean flag = this.mc.isIntegratedServerRunning();
            button.enabled = false;
            this.mc.world.sendQuittingDisconnectingPacket();
            this.mc.loadWorld(null);
            if (flag) {
                this.mc.displayGuiScreen(new GuiMainMenu());
            } else {
                this.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        int marginLeft = com.theyausebox.yause.config.YauseMenuConfig.menuOffsetX;
        int marginTop = 10;
        int marginBottom = 0;
        int titleSpacing = 8;
        int boxX = marginLeft;
        int boxY = marginTop;
        int boxWidth = 200;
        int boxHeight = this.height - marginTop - marginBottom;

        float openProgress = 1.0f;
        if (this.isClosing) {
            long elapsedMs = net.minecraft.client.Minecraft.getSystemTime() - this.closeStartTimeMs;
            float p = Math.min(1.0f, (float)elapsedMs / (float)this.closeDurationMs);

            float eased = (p < 0.5f) ? (4.0f * p * p * p) : (1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3) / 2.0f);
            openProgress = 1.0f - eased;
            if (p >= 1.0f) {

                this.isClosing = false;
                this.openStartTimeMs = -1;
                this.closeStartTimeMs = -1;

                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();
                return;
            }
        } else if (this.openStartTimeMs >= 0) {
            long elapsedMs = net.minecraft.client.Minecraft.getSystemTime() - this.openStartTimeMs;
            float p = Math.min(1.0f, (float)elapsedMs / (float)this.openDurationMs);

            openProgress = (p < 0.5f) ? (4.0f * p * p * p) : (1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3) / 2.0f);
        }

        if (com.theyausebox.yause.config.YauseMenuConfig.disableTransitions) {
            openProgress = 1.0f;
        }

        currentOpenProgress = openProgress;

        int boxXFinal = boxX;
        int slideDistance = boxWidth + boxXFinal;

        float translateXFloat = -((1.0f - openProgress) * (float)slideDistance);

        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(translateXFloat, 0f, 0f);
        int baseAlpha = 0xB0;
        int alphaFade = Math.round(baseAlpha * openProgress);
        int bgColor = (alphaFade << 24) | 0x000000;

        if (boxY > 0) {
            drawRect(boxX, 0, boxX + boxWidth, boxY, bgColor);
        }
        drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, bgColor);

        int borderAlpha = Math.round(255 * openProgress);
        int borderColor = (borderAlpha << 24) | 0xFFFFFF;

        drawRect(boxX, 0, boxX + 2, boxY + boxHeight, borderColor);

        int shadowAlpha = Math.round(24 * openProgress);
        if (shadowAlpha > 0) {
            int shadowColor = (shadowAlpha << 24) | 0x000000;
            drawRect(boxX + boxWidth, boxY + 2, boxX + boxWidth + 4, boxY + boxHeight - 2, shadowColor);
        }

        String title = I18n.format("menu.game");
        String displayTitle = title;
        int titleX = boxX + 14;
            int titleY = boxY + 28;

        float scale = 2.0f;
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate((float)titleX, (float)titleY, 0f);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1f);
        int titleAlpha = Math.round(255 * openProgress);
        int titleColor = (titleAlpha << 24) | 0xFFFFFF;
        this.fontRenderer.drawStringWithShadow(displayTitle, 0f, 0f, titleColor);
        net.minecraft.client.renderer.GlStateManager.popMatrix();

        int infoX = boxX + 14;
        int titleHeightPx = (int)(this.fontRenderer.FONT_HEIGHT * 2.0f);
        int infoStartY = titleY + titleHeightPx + 6;

        boolean drewFTB = false;
        if (YauseMenuConfig.enableQuests && com.theyausebox.yause.YauseMenu.ftbQuestsInstalled) {
            if (this.cachedFTBHasActive && this.cachedFTBText != null) {
                int infoColor = (int) (0xCC * openProgress) << 24 | 0x88CCFF;
                this.fontRenderer.drawStringWithShadow(this.cachedFTBText, infoX, infoStartY, infoColor);
                drewFTB = true;
            } else {
                String hintKey = "yause.ftbquests.unavailable";
                String hint = I18n.format(hintKey);
                if (hint != null && hint.equals(hintKey)) {
                    hint = "No active chapter";
                }
                int hintColor = (int) (0xAA * openProgress) << 24 | 0x888888;
                this.fontRenderer.drawStringWithShadow(hint, infoX, infoStartY, hintColor);
                drewFTB = true;
            }
        }

        if (YauseMenuConfig.showPlaytime && this.mc.player != null) {
            Long ticks = this.cachedVanillaPlayTicks;
            if (ticks != null) {
                long playSeconds = ticks / 20L;
                String playtimeStr = formatPlaytime(playSeconds);
                int infoColor = ((int)(0xCC * openProgress) << 24) | 0x999999;
                int playY = infoStartY + (drewFTB ? (this.fontRenderer.FONT_HEIGHT + 4) : 0);
                this.fontRenderer.drawStringWithShadow("Time played: " + playtimeStr.replace("Playtime: ", ""), infoX, playY, infoColor);
            }
        }

        if (this.buttonPanelLeft != null) {
            this.buttonPanelLeft.updatePosition(this.width, this.height);

            int mouseAdjX = mouseX - Math.round(translateXFloat);
            this.buttonPanelLeft.updateButtons(this.updateCounter, partialTicks, mouseAdjX, mouseY);
        }

        for (GuiButton button : this.buttonList) {

            button.drawButton(this.mc, mouseX - Math.round(translateXFloat), mouseY, partialTicks);
        }
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    private String formatPlaytime(long seconds) {
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d");
            if (hours > 0) sb.append(' ').append(hours).append("h");
            if (minutes > 0) sb.append(' ').append(minutes).append("m");
            if (secs > 0) sb.append(' ').append(secs).append("s");
        } else if (hours > 0) {
            sb.append(hours).append("h");
            if (minutes > 0) sb.append(' ').append(minutes).append("m");
            if (secs > 0) sb.append(' ').append(secs).append("s");
        } else if (minutes > 0) {
            sb.append(minutes).append("m");
            if (secs > 0) sb.append(' ').append(secs).append("s");
        } else {
            sb.append(secs).append("s");
        }

        return sb.toString();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {

        int boxX = com.theyausebox.yause.config.YauseMenuConfig.menuOffsetX;
        int slideDistance = 200 + boxX;
        float openProgress = 1.0f;
        if (this.openStartTimeMs >= 0) {
            long elapsedMs = net.minecraft.client.Minecraft.getSystemTime() - this.openStartTimeMs;
            float p = Math.min(1.0f, (float)elapsedMs / (float)this.openDurationMs);
            openProgress = (p < 0.5f) ? (4.0f * p * p * p) : (1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3) / 2.0f);
        }
        int translateX = Math.round(-((1.0f - openProgress) * slideDistance));
        super.mouseClicked(mouseX - translateX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        int boxX = com.theyausebox.yause.config.YauseMenuConfig.menuOffsetX;
        int slideDistance = 200 + boxX;
        float openProgress = 1.0f;
        if (this.openStartTimeMs >= 0) {
            long elapsedMs = net.minecraft.client.Minecraft.getSystemTime() - this.openStartTimeMs;
            float p = Math.min(1.0f, (float)elapsedMs / (float)this.openDurationMs);
            openProgress = (p < 0.5f) ? (4.0f * p * p * p) : (1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3) / 2.0f);
        }
        int translateX = Math.round(-((1.0f - openProgress) * slideDistance));
        super.mouseReleased(mouseX - translateX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {

        if (keyCode == 1) {
            if (com.theyausebox.yause.config.YauseMenuConfig.disableTransitions) {
                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();
                return;
            }
            if (!this.isClosing) {
                this.isClosing = true;
                this.closeStartTimeMs = net.minecraft.client.Minecraft.getSystemTime();
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {

        this.cachedFTBText = null;

        this.cachedVanillaPlayTicks = null;
        this.lastVanillaReadMs = 0L;
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ++this.updateCounter;

        if (YauseMenuConfig.showPlaytime && this.mc.player != null) {

            long now = net.minecraft.client.Minecraft.getSystemTime();
            if (now - this.lastVanillaReadMs >= 1000L) {
                this.cachedVanillaPlayTicks = getVanillaPlayTicks();
                this.lastVanillaReadMs = now;
            }
        }

        if (this.openStartTimeMs >= 0 && !this.isClosing) {
            long now = net.minecraft.client.Minecraft.getSystemTime();
            if (YauseMenuConfig.enableQuests && com.theyausebox.yause.YauseMenu.ftbQuestsInstalled) {
                updateFTBCache();
            }
        }
    }
}

