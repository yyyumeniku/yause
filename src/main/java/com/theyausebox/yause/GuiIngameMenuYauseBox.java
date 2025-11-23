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
// We no longer depend on vanilla StatList for FTBU reads — prefer FTBU keys and tolerant fallbacks.
import com.theyausebox.yause.config.YauseMenuConfig;

public class GuiIngameMenuYauseBox extends GuiIngameMenu {
    private GuiButtonPanel buttonPanelLeft;
    private int updateCounter = 0;
    private long openStartTimeMs = -1; // start time in ms for time-based animation
    private int openDurationMs = com.theyausebox.yause.config.YauseMenuConfig.openAnimationMs; // open duration in ms (time-based)
    public static float currentOpenProgress = 1.0f; // used by buttons to fade during open
    // closing (fade-out) animation
    private boolean isClosing = false;
    private long closeStartTimeMs = -1;
    private int closeDurationMs = com.theyausebox.yause.config.YauseMenuConfig.closeAnimationMs;
    // Cached vanilla playtime (ticks) read from the client's statistics manager.
    // We refresh this once per second while the menu is open so the UI matches
    // the vanilla Statistics screen exactly (no local session offsets).
    private Long cachedVanillaPlayTicks = null;
    private long lastVanillaReadMs = 0L; // last time the cached stat was refreshed
    // cached info from FTB-Quests and FTB-Utilities
    private String cachedFTBText = null;
    private boolean cachedFTBHasActive = false;
    private static boolean ftbQuestsUnavailableLogged = false;
    private static boolean ftbQuestsMissingInstanceLogged = false;
    private static boolean ftbQuestsMissingSelfLogged = false;
    // (Removed unused start-related probe flags — we do not read per-quest started lists anymore)
    // All FTBU/vanilla playtime logic removed

    @Override
    public void initGui() {
        this.buttonList.clear();
        // Vertical box margins and title spacing - anchored top-left
            int marginLeft = com.theyausebox.yause.config.YauseMenuConfig.menuOffsetX; // shift panel a few pixels to the right for final alignment
            // `menuOffsetY` config option removed — use a fixed vertical offset instead.
            int marginTop = 10; // vertical offset (fixed)
            int marginBottom = 0; // full height
            int titleSpacing = 4; // smaller spacing to fill the top area better
            int buttonTopPadding = 0; // no extra gap between title and the top-most button
        int boxWidth = 200;
        int boxHeight = this.height - marginTop - marginBottom;

        // Determine title and compute the vertical space for the panel below the title
        int titleHeight = this.fontRenderer.FONT_HEIGHT;
            // We'll anchor the button panel to the bottom-left so the buttons sit low in the box.
            float scale = 2.0f;
            int titleDrawHeight = (int)(titleHeight * scale);
            int buttonBottomPadding = 4; // smaller bottom padding so the buttons panel grows to fill the top area
            // Keep the panel height large enough to cover the box from the title downwards (fills the vertical gap)
            int panelHeight = Math.max(48, boxHeight - titleDrawHeight - titleSpacing - buttonBottomPadding - buttonTopPadding); // ensure a minimum height
            // Calculate panel offset so when anchored to the bottom it lines up neatly under the title and fills the box
            int panelTopDesired = marginTop + titleDrawHeight + titleSpacing + buttonTopPadding; // top edge we want the panel to start at
            // Use exact desired top position based on title area (avoid nudge) so top area is filled
            int panelYOffset = Math.max(0, this.height - panelHeight - panelTopDesired); // yOffset for bottom anchor

        // Initialize (or recreate) button panel (top-left anchored to fill the vertical box)
        int panelWidth = boxWidth - 8; // smaller than box to provide padding
            if (this.buttonPanelLeft == null || this.buttonPanelLeft.getPanelWidth() != panelWidth || this.buttonPanelLeft.getPanelHeight() != panelHeight || this.buttonPanelLeft.getYOffset() != panelYOffset) {
                // Anchor BottomLeft to restore the buttons to their previous vertical positions while filling the gap above
                this.buttonPanelLeft = new GuiButtonPanel(100, GuiButtonPanel.AnchorType.BottomLeft,
                marginLeft + 10, panelYOffset, panelWidth, panelHeight, 20, this.width, this.height, "left");
            this.initPanelButtons();
        } else {
            this.buttonPanelLeft.updatePosition(this.width, this.height);
        }

        this.buttonList.add(this.buttonPanelLeft);
        // Start slide-in animation (time-based) so the pause menu slides in from the left
        this.openStartTimeMs = net.minecraft.client.Minecraft.getSystemTime();
        // Update durations from config in case user changed them in the config screen
        this.openDurationMs = com.theyausebox.yause.config.YauseMenuConfig.openAnimationMs;
        this.closeDurationMs = com.theyausebox.yause.config.YauseMenuConfig.closeAnimationMs;
        // Initialize cached vanilla stat (best-effort) and set last read time
        this.cachedVanillaPlayTicks = null;
        this.lastVanillaReadMs = net.minecraft.client.Minecraft.getSystemTime();
        if (YauseMenuConfig.showPlaytime && this.mc != null && this.mc.player != null) {
            this.cachedVanillaPlayTicks = getVanillaPlayTicks();
        }

        if (YauseMenuConfig.enableQuests && com.theyausebox.yause.YauseMenu.ftbQuestsInstalled) {
            updateFTBCache();
        }
        // (playtime removed) updateFTBCache will still be used for FTB Quests hints
    }

    // Cache FTB quest info once per menu open so we avoid reflection overhead
    private void updateFTBCache() {
        this.cachedFTBText = null;
        this.cachedFTBHasActive = false;

        try {
            Class<?> clientQuestFileCls = null;
            // Try real mod package first, then fall back to our internal stub package so we don't create
            // runtime class collisions when the real mod exists in the dev environment.
            try {
                clientQuestFileCls = Class.forName("com.feed_the_beast.ftbquests.client.ClientQuestFile");
            } catch (ClassNotFoundException ex) {
                try {
                    clientQuestFileCls = Class.forName("com.theyausebox.yause.internal.ftbquests.client.ClientQuestFile");
                } catch (ClassNotFoundException ex2) {
                    throw ex; // rethrow original so outer catch handles unavailability
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

            // Prefer reading per-player visible chapters and their progress (FTB Quests client API)
            try {
                Class<?> questDataCls = null;
                try {
                    questDataCls = Class.forName("com.feed_the_beast.ftbquests.quest.QuestData");
                } catch (ClassNotFoundException ex) {
                    // if the real mod types aren't present, try our internal stub
                    questDataCls = Class.forName("com.theyausebox.yause.internal.ftbquests.quest.QuestData");
                }
                java.lang.reflect.Method visibleChaptersMethod = null;
                // method signature in QuestFile: List<Chapter> getVisibleChapters(QuestData data, boolean excludeEmpty)
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
                                // Pick the first chapter that has progress > 0 and < 100 — that's the active one
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
                                    return; // done — we found an active chapter
                                }
                            }
                        }
                    } catch (Throwable ignore) {}
                }
            } catch (Throwable ignore) {}

            // We prefer showing CHAPTER information only. If we couldn't find an active chapter
            // we don't fall back to per-quest data — display a clear 'No active chapter' message instead.
            // Represent an explicit 'no active chapter' state in the cached text — keep as displayable
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
    

    // =========================
    // Vanilla playtime helpers
    // =========================

    // Read vanilla playtime (StatList.PLAY_ONE_MINUTE) from the client's stats manager.
    // Returns ticks or null if unavailable.
    private Long getVanillaPlayTicks() {
        try {
            if (this.mc == null || this.mc.player == null) return null;
            net.minecraft.stats.StatBase stat = net.minecraft.stats.StatList.PLAY_ONE_MINUTE;
            if (stat == null) return null;

            // Try the direct API first (MCP mapping): StatFileWriter.readStat(StatBase)
            try {
                Object statsManagerDirect = this.mc.player.getStatFileWriter();
                if (statsManagerDirect != null) {
                    try {
                        java.lang.reflect.Method readStat = statsManagerDirect.getClass().getMethod("readStat", net.minecraft.stats.StatBase.class);
                        Object val = readStat.invoke(statsManagerDirect, stat);
                        if (val instanceof Number) return ((Number) val).longValue();
                        if (val != null) return Long.parseLong(val.toString());
                    } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                        // fall through to reflection approach below
                    }
                }
            } catch (Throwable ignored) {
                // fall through to reflection approach below
            }

            Object statsManager = null;
            try {
                // fallback: try to access likely field names via reflection
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
                    // Best effort: scan for a single-arg method that accepts something named like Stat
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
        // All buttons on left panel
        this.buttonPanelLeft.addButton(I18n.format("menu.returnToGame"));
        this.buttonPanelLeft.addButton(I18n.format("gui.advancements"));
        // Restore the vanilla Stats button so users can open the full stats screen
        this.buttonPanelLeft.addButton(I18n.format("gui.stats"));
        // Removed vanilla stats button - playtime display is FTBU-only
        // Removed feedback & report buttons per user's request
        this.buttonPanelLeft.addButton(I18n.format("menu.options"));
        // Mods button (opens Forge mod list)
        this.buttonPanelLeft.addButton(I18n.format("fml.menu.mods"));
        
        if (this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic()) {
            this.buttonPanelLeft.addButton(I18n.format("menu.shareToLan"));
        }

        this.buttonPanelLeft.addButton(I18n.format(this.mc.isIntegratedServerRunning() ? "menu.returnToMenu" : "menu.disconnect"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // Get the actual button from the panel if it's a panel click
        if (button.id == this.buttonPanelLeft.id) {
            button = this.buttonPanelLeft.getPressedButton();
        }
        
        // Match button by its localization key (displayString)
        String buttonText = button.displayString;
        
            if (buttonText.equals(I18n.format("menu.returnToGame"))) {
            // If transitions are disabled, close instantly. Otherwise start fade-out closing animation.
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
            // Open Forge's mod list GUI — use helper to handle compatibility
            boolean ok = com.theyausebox.yause.ForgeHandler.openModsList(this.mc, this);
            if (!ok) {
                com.theyausebox.yause.YauseMenu.LOGGER.warn("Failed to open mod list GUI via ForgeHandler");
            }
        } else if (buttonText.equals(I18n.format("gui.advancements"))) {
            this.mc.displayGuiScreen(new GuiScreenAdvancements(this.mc.player.connection.getAdvancementManager()));
        } else if (buttonText.equals(I18n.format("gui.stats"))) {
            try {
                // Open the vanilla stats screen (MCP: GuiStats / obfuscated may differ); use known class name for 1.12
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
        // Don't draw default darkened background - we want transparency
        // Box and panel metrics (flush top-left box) - keep consistent with initGui
        int marginLeft = com.theyausebox.yause.config.YauseMenuConfig.menuOffsetX; // moved to the right by configured amount
        // Vertical offset config removed — use a constant value for vertical placement
        int marginTop = 10;
        int marginBottom = 0;
        int titleSpacing = 8;
        int boxX = marginLeft;
        int boxY = marginTop;
        int boxWidth = 200;
        int boxHeight = this.height - marginTop - marginBottom;

        // Compute open animation progress (slide from left)
        float openProgress = 1.0f;
        if (this.isClosing) {
            long elapsedMs = net.minecraft.client.Minecraft.getSystemTime() - this.closeStartTimeMs;
            float p = Math.min(1.0f, (float)elapsedMs / (float)this.closeDurationMs);
            // Smooth ease-in-out cubic for close (mirror of open) — just invert p's result
            float eased = (p < 0.5f) ? (4.0f * p * p * p) : (1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3) / 2.0f);
            openProgress = 1.0f - eased;
            if (p >= 1.0f) {
                // close complete — return to game
                this.isClosing = false;
                this.openStartTimeMs = -1;
                this.closeStartTimeMs = -1;
                // call original close action
                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();
                return; // stop draw; parent will close the GUI
            }
        } else if (this.openStartTimeMs >= 0) {
            long elapsedMs = net.minecraft.client.Minecraft.getSystemTime() - this.openStartTimeMs;
            float p = Math.min(1.0f, (float)elapsedMs / (float)this.openDurationMs);
            // Smooth ease-in-out (cubic) for very smooth motion
            openProgress = (p < 0.5f) ? (4.0f * p * p * p) : (1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3) / 2.0f);
        }

        // If transitions are disabled in config, skip animations entirely
        if (com.theyausebox.yause.config.YauseMenuConfig.disableTransitions) {
            openProgress = 1.0f;
        }

        // Keep progress available for draw/hover fade
        currentOpenProgress = openProgress;

        // Apply slide translation: move left when openProgress < 1, start off-screen left
        int boxXFinal = boxX;
        int slideDistance = boxWidth + boxXFinal; // translate from left off-screen
        // Use float translation for smooth per-frame movement; don't round until mapping the mouse
        float translateXFloat = -((1.0f - openProgress) * (float)slideDistance);

        // Dark background with transparency (vertical box) - heavier to match the screenshot
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(translateXFloat, 0f, 0f);
        int baseAlpha = 0xB0; // 176
        int alphaFade = Math.round(baseAlpha * openProgress);
        int bgColor = (alphaFade << 24) | 0x000000;
        // Fill the top area above the box too to avoid the thin gap shown on macOS when menu is nudged down
        if (boxY > 0) {
            drawRect(boxX, 0, boxX + boxWidth, boxY, bgColor);
        }
        drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, bgColor); // more opaque black

        // White left border only
        int borderAlpha = Math.round(255 * openProgress);
        int borderColor = (borderAlpha << 24) | 0xFFFFFF;
        // Extend the white left border up to the top to match the background fill above
        drawRect(boxX, 0, boxX + 2, boxY + boxHeight, borderColor); // Left border

        // Subtle shadow to the right for visual polish (small, faded, and follows open progress)
        int shadowAlpha = Math.round(24 * openProgress); // gentle shadow
        if (shadowAlpha > 0) {
            int shadowColor = (shadowAlpha << 24) | 0x000000;
            drawRect(boxX + boxWidth, boxY + 2, boxX + boxWidth + 4, boxY + boxHeight - 2, shadowColor);
        }

        // Draw "Game Menu" title - normal weight, left-aligned
        String title = I18n.format("menu.game");
        String displayTitle = title;
        int titleX = boxX + 14; // moved 4px to the right
            int titleY = boxY + 28; // move the title down slightly to better align with the filled top area
        // Increase the title size by scaling the draw call
        float scale = 2.0f;
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate((float)titleX, (float)titleY, 0f);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1f);
        int titleAlpha = Math.round(255 * openProgress);
        int titleColor = (titleAlpha << 24) | 0xFFFFFF;
        this.fontRenderer.drawStringWithShadow(displayTitle, 0f, 0f, titleColor);
        net.minecraft.client.renderer.GlStateManager.popMatrix();

        // Base X/Y for the informational area directly below the title
        int infoX = boxX + 14;
        int titleHeightPx = (int)(this.fontRenderer.FONT_HEIGHT * 2.0f);
        int infoStartY = titleY + titleHeightPx + 6;

        // First draw the optional FTB-Quests chapter/hint at infoStartY. If we drew
        // a chapter/hint we'll shift the 'Time played' stat below it to avoid overlap.
        boolean drewFTB = false;
        if (YauseMenuConfig.enableQuests && com.theyausebox.yause.YauseMenu.ftbQuestsInstalled) {
            if (this.cachedFTBHasActive && this.cachedFTBText != null) {
                int infoColor = (int) (0xCC * openProgress) << 24 | 0x88CCFF; // blueish hint for FTB
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

        // Draw the vanilla 'Time played' stat directly beneath the chapter/hint area
        if (YauseMenuConfig.showPlaytime && this.mc.player != null) {
            Long ticks = this.cachedVanillaPlayTicks;
            if (ticks != null) {
                long playSeconds = ticks / 20L; // vanilla stat stores ticks
                String playtimeStr = formatPlaytime(playSeconds);
                int infoColor = ((int)(0xCC * openProgress) << 24) | 0x999999;
                int playY = infoStartY + (drewFTB ? (this.fontRenderer.FONT_HEIGHT + 4) : 0);
                this.fontRenderer.drawStringWithShadow("Time played: " + playtimeStr.replace("Playtime: ", ""), infoX, playY, infoColor);
            }
        }

        // When FTBU isn't installed and the playtime feature is enabled, show a short
        // hint telling the user playtime requires FTBU. This is controlled by the
        // Playtime display is controlled by YauseMenuConfig.showPlaytime and requires FTB Utilities at runtime
        // warning flags.
        // The explicit short 'FTBU required' message has been removed from the config
        // schema; the menu no longer displays this separate hint.

        // Update and draw custom buttons inside the box
        // Small brand overlay to hide residual bottom-left brand text without covering large parts of the view
        // Make the overlay cover most of our menu so text doesn't overlap with the panel buttons
        // Expand the overlay a little to the left and right to make sure it hides leftover text in lower-left.
        // Remove the bottom-left brand overlay — it created an unwanted rectangle in the screenshot.
        // We do not draw the small overlay at the bottom-left anymore.
        // Update and draw custom panel buttons with coordinates adjusted for translation
        if (this.buttonPanelLeft != null) {
            this.buttonPanelLeft.updatePosition(this.width, this.height);
            // Pass mouse coordinates translated into the open progress coordinate space
            int mouseAdjX = mouseX - Math.round(translateXFloat);
            this.buttonPanelLeft.updateButtons(this.updateCounter, partialTicks, mouseAdjX, mouseY);
        }

        // Draw all buttons
        for (GuiButton button : this.buttonList) {
            // When drawing, the GL matrix already translated the panel drawings above; we still
            // want drawButton to receive mouse coordinates in the untranslated coordinate space
            // (we've already adjusted panel updates), therefore the raw coords are fine here.
            button.drawButton(this.mc, mouseX - Math.round(translateXFloat), mouseY, partialTicks);
        }
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    // Format seconds into a human friendly string, e.g. "Playtime: 2d 3h 12m 5s".
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

    // FTBU reflection helpers removed.
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        // Adjust mouse x for the open animation translate so clicks map to drawn positions
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
        // ESC (keyCode 1) triggers the close animation as well — if transitions are disabled,
        // close immediately.
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
        // Release cached values to free small amounts of memory (strings, boxed Longs)
        this.cachedFTBText = null;
        // Clear cached vanilla playtime so the next open reads fresh
        this.cachedVanillaPlayTicks = null;
        this.lastVanillaReadMs = 0L;
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ++this.updateCounter;
        // Increment session tick counter while pause menu is open so playtime updates in real-time.
        // Only increment when the game is not paused (so we don't add ticks during single-player pause).
        if (YauseMenuConfig.showPlaytime && this.mc.player != null) {
            // Refresh the cached vanilla stat once per second while the pause menu
            // is open so the displayed value updates in near real-time.
            long now = net.minecraft.client.Minecraft.getSystemTime();
            if (now - this.lastVanillaReadMs >= 1000L) {
                this.cachedVanillaPlayTicks = getVanillaPlayTicks();
                this.lastVanillaReadMs = now;
            }
        }

        // Periodic refresh of FTBU play ticks and FTB-Quests cache while the menu is open (reduce reflection frequency)
        if (this.openStartTimeMs >= 0 && !this.isClosing) {
            long now = net.minecraft.client.Minecraft.getSystemTime();
            if (YauseMenuConfig.enableQuests && com.theyausebox.yause.YauseMenu.ftbQuestsInstalled) {
                updateFTBCache();
            }
        }
    }
}



















