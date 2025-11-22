package com.thevoxelbox.yause;

import com.thevoxelbox.yause.controls.GuiButtonPanel;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.resources.I18n;
import net.minecraft.stats.StatList;
import com.thevoxelbox.yause.config.VoxelMenuConfig;

public class GuiIngameMenuVoxelBox extends GuiIngameMenu {
    private GuiButtonPanel buttonPanelLeft;
    private int updateCounter = 0;
    private long openStartTimeMs = -1; // start time in ms for time-based animation
    private int openDurationMs = com.thevoxelbox.yause.config.VoxelMenuConfig.openAnimationMs; // open duration in ms (time-based)
    public static float currentOpenProgress = 1.0f; // used by buttons to fade during open
    // closing (fade-out) animation
    private boolean isClosing = false;
    private long closeStartTimeMs = -1;
    private int closeDurationMs = com.thevoxelbox.yause.config.VoxelMenuConfig.closeAnimationMs;
    // Playtime tracking while pause menu is open so it updates live
    // -1 indicates no FTBU playtime value has been captured (we *never* use vanilla stats)
    private long basePlayTicks = -1L;
    private long sessionPlayTicks = 0;
    // cached info from FTB-Quests and FTB-Utilities
    private String cachedFTBText = null;
    private boolean cachedFTBHasActive = false;
    private static boolean ftbQuestsUnavailableLogged = false;
    private static boolean ftbQuestsMissingInstanceLogged = false;
    private static boolean ftbQuestsMissingSelfLogged = false;
    // (Removed unused start-related probe flags — we do not read per-quest started lists anymore)
    // FTBU reflection caches — we'll try to get Universe.get() and Universe.getPlayer(player)
    private static boolean ftbuInitTried = false;
    // If the optional FTBLib/FTBU classes weren't available during the first probe
    // the classloader may not have finished wiring things up yet in a dev env.
    // Allow a retry every few seconds while the mod is installed.
    private static long ftbuLastInitAttemptMs = 0L;
    private static final long FTBU_INIT_RETRY_MS = 5000L; // retry every 5s when needed
    private static Class<?> ftbuUniverseClass = null;
    private static java.lang.reflect.Method ftbuUniverseGet = null;
    private static java.lang.reflect.Method ftbuUniverseGetPlayer = null;
    private static Class<?> ftbuForgePlayerClass = null;
    private static java.lang.reflect.Method ftbuStatsMethod = null; // method on ForgePlayer to get stats
    private static java.lang.reflect.Method ftbuReadStatMethod = null; // method on stats object to get a stat value
    private static boolean ftbuUnavailableLogged = false;
    private static boolean ftbuPlaytimeMissingLogged = false;
    // One-time diagnostic dump if FTBU reflection doesn't match our expectations — useful when many mods are present
    private static boolean ftbuProbeDumpLogged = false;
    // (Removed unused FTBU vanilla fallback logging flag)
    // additional FTBU helper: some versions expose a static ForgePlayer getter rather than Universe.get()
    private static java.lang.reflect.Method ftbuForgePlayerStaticGetter = null;
    // cached FTBU play ticks (reads once every refresh interval)
    private Long cachedFTBUPlayTicks = null;
    private long lastFTBRefreshMs = 0L;
    private static final long FTB_REFRESH_INTERVAL_MS = 2000L; // refresh every 2s while menu is open

    @Override
    public void initGui() {
        this.buttonList.clear();
        // Vertical box margins and title spacing - anchored top-left
            int marginLeft = com.thevoxelbox.yause.config.VoxelMenuConfig.menuOffsetX; // shift panel a few pixels to the right for final alignment
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
        this.openDurationMs = com.thevoxelbox.yause.config.VoxelMenuConfig.openAnimationMs;
        this.closeDurationMs = com.thevoxelbox.yause.config.VoxelMenuConfig.closeAnimationMs;
        // Reset playtime base on open so the menu shows live playtime while open
        try {
            // Only populate basePlayTicks when the user has enabled FTBU-based playtime
            // and FTBU is present. Do not fall back to vanilla playtime; when FTBU is
            // missing or the feature is disabled we leave it zero so no playtime displays.
            if (VoxelMenuConfig.showPlaytime && com.thevoxelbox.yause.VoxelMenu.ftbUtilitiesInstalled) {
                // If we've previously tried to initialize FTBU reflection and it failed,
                // the 'ftbuInitTried' flag can prevent an immediate re-probe. When the
                // user opens the menu and playtime is requested *and* FTBU is present,
                // force a fresh probe so UI shows playtime immediately rather than
                // relying on the periodic retry window.
                if (ftbuInitTried && ftbuUniverseClass == null && ftbuForgePlayerClass == null && ftbuForgePlayerStaticGetter == null) {
                    ftbuInitTried = false;
                    ftbuLastInitAttemptMs = 0L;
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU: forcing reflection re-probe on menu open");
                }
                Long v = getFTBUPlayTicks();
                if (v == null) {
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.info("Menu open: FTBU installed but playtime read returned null (ftbuInitTried={}, ftbuUnavailableLogged={}, ftbuPlaytimeMissingLogged={})", ftbuInitTried, ftbuUnavailableLogged, ftbuPlaytimeMissingLogged);
                }
                // Do NOT fall back to vanilla playtime. If FTBU isn't available mark base as unavailable (-1)
                this.basePlayTicks = v == null ? -1L : v.longValue();
            } else {
                // When FTBU is not installed or disabled, mark as unavailable
                this.basePlayTicks = -1L;
            }
        } catch (Throwable ignored) {
            this.basePlayTicks = -1L;
        }
        this.sessionPlayTicks = 0;
        // Update FTB-Quests cache and playtime stats once at menu open so we don't reflect each frame
        if (VoxelMenuConfig.showPlaytime) {
            Long v = getFTBUPlayTicks();
            if (v != null) cachedFTBUPlayTicks = v;
        } else {
            this.cachedFTBUPlayTicks = null;
        }

        if (VoxelMenuConfig.enableQuests && com.thevoxelbox.yause.VoxelMenu.ftbQuestsInstalled) {
            updateFTBCache();
        }
        this.lastFTBRefreshMs = net.minecraft.client.Minecraft.getSystemTime();
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
                    clientQuestFileCls = Class.forName("com.thevoxelbox.yause.internal.ftbquests.client.ClientQuestFile");
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
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTB Quests: ClientQuestFile.INSTANCE is null (no client instance available)");
                }
            }

            Object self = selfField.get(instance);
            if (self == null) {
                if (!ftbQuestsMissingSelfLogged) {
                    ftbQuestsMissingSelfLogged = true;
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTB Quests: ClientQuestFile.self is null (no per-player quest data available)");
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
                    questDataCls = Class.forName("com.thevoxelbox.yause.internal.ftbquests.quest.QuestData");
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
                com.thevoxelbox.yause.VoxelMenu.LOGGER.warn("FTB Quests reflection failed while reading quests: {}", msg);
            }
        }
    }
    

    // Update cached FTBU play ticks explicitly (fills cachedFTBUPlayTicks) and returns the value.
    private Long refreshFTBUPlayTicks() {
        Long v = getFTBUPlayTicks();
        if (v != null) {
            this.cachedFTBUPlayTicks = v;
            this.lastFTBRefreshMs = net.minecraft.client.Minecraft.getSystemTime();
        }
        return v;
    }

    private void initPanelButtons() {
        // All buttons on left panel
        this.buttonPanelLeft.addButton(I18n.format("menu.returnToGame"));
        this.buttonPanelLeft.addButton(I18n.format("gui.advancements"));
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
            if (com.thevoxelbox.yause.config.VoxelMenuConfig.disableTransitions) {
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
            boolean ok = com.thevoxelbox.yause.ForgeHandler.openModsList(this.mc, this);
            if (!ok) {
                com.thevoxelbox.yause.VoxelMenu.LOGGER.warn("Failed to open mod list GUI via ForgeHandler");
            }
        } else if (buttonText.equals(I18n.format("gui.advancements"))) {
            this.mc.displayGuiScreen(new GuiScreenAdvancements(this.mc.player.connection.getAdvancementManager()));
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
        int marginLeft = com.thevoxelbox.yause.config.VoxelMenuConfig.menuOffsetX; // moved to the right by configured amount
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
        if (com.thevoxelbox.yause.config.VoxelMenuConfig.disableTransitions) {
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

        // Optional playtime stat below the title (FTBU-only)
        if (VoxelMenuConfig.showPlaytime && this.mc.player != null && com.thevoxelbox.yause.VoxelMenu.ftbUtilitiesInstalled) {
            int statsX = boxX + 14;
            int titleHeightPx = (int)(this.fontRenderer.FONT_HEIGHT * 2.0f);
            int playY = titleY + titleHeightPx + 6;

            // Prefer cached FTBU play ticks, otherwise use the base (taken at menu open) if available
            Long baseTicksObj = (this.cachedFTBUPlayTicks != null) ? this.cachedFTBUPlayTicks : (this.basePlayTicks >= 0L ? Long.valueOf(this.basePlayTicks) : null);

            if (baseTicksObj == null) {
                if (!ftbuPlaytimeMissingLogged) {
                    ftbuPlaytimeMissingLogged = true;
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.info("Yause: FTBU playtime not available in menu (no FTBU value present). ftbUtilitiesInstalled={}, ftbuInitTried={}, ftbuUnavailableLogged={}", com.thevoxelbox.yause.VoxelMenu.ftbUtilitiesInstalled, ftbuInitTried, ftbuUnavailableLogged);
                }
            } else {
                long baseTicks = baseTicksObj.longValue();
                if (baseTicks < 0L) {
                    if (!ftbuPlaytimeMissingLogged) {
                        ftbuPlaytimeMissingLogged = true;
                        com.thevoxelbox.yause.VoxelMenu.LOGGER.info("Yause: FTBU playtime not available (base ticks < 0). Skipping display.");
                    }
                } else {
                    long playTicks = baseTicks + this.sessionPlayTicks;
                    long playSeconds = playTicks / 20L;
                    String playtimeStr = formatPlaytime(playSeconds);
                    int infoColor = (int) (0xCC * openProgress) << 24 | 0x999999;
                    this.fontRenderer.drawStringWithShadow(playtimeStr, statsX, playY, infoColor);
                }
            }
        }

        // Optional FTB-Quests integration (soft, reflection-based) — we cache results on open to reduce overhead
        if (VoxelMenuConfig.enableQuests && com.thevoxelbox.yause.VoxelMenu.ftbQuestsInstalled) {
            // compute common y coordinate for the FTB message/hint area
            int ftbY = boxY + 28 + (int)(this.fontRenderer.FONT_HEIGHT * 2.0f) + 6;
            if (VoxelMenuConfig.showPlaytime) {
                ftbY += this.fontRenderer.FONT_HEIGHT + 4;
            }
            if (this.cachedFTBHasActive && this.cachedFTBText != null) {
                int infoColor = (int) (0xCC * openProgress) << 24 | 0x88CCFF; // blueish hint for FTB
                this.fontRenderer.drawStringWithShadow(this.cachedFTBText, boxX + 14, ftbY, infoColor);
            }
            // If integration is enabled and the mod is installed, but we didn't find quests, show a short hint
            else if (VoxelMenuConfig.enableQuests && com.thevoxelbox.yause.VoxelMenu.ftbQuestsInstalled) {
                int hintY = ftbY;
                String hintKey = "yause.ftbquests.unavailable";
                String hint = I18n.format(hintKey);
                // I18n.format returns the key verbatim when a translation is missing — fall back to an English message
                if (hint != null && hint.equals(hintKey)) {
                    hint = "No active chapter";
                }
                int hintColor = (int) (0xAA * openProgress) << 24 | 0x888888;
                this.fontRenderer.drawStringWithShadow(hint, boxX + 14, hintY, hintColor);
            }
        }

        // When FTBU isn't installed and the playtime feature is enabled, show a short
        // hint telling the user playtime requires FTBU. This is controlled by the
        // Playtime display is controlled by VoxelMenuConfig.showPlaytime and requires FTB Utilities at runtime
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

    // Human-friendly playtime formatting: e.g., "2d 3h 12m 5s" or "5m 20s" (always include seconds when relevant)
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
            // less than a minute — always show seconds
            sb.append(secs).append("s");
        }

        return "Playtime: " + sb.toString();
    }

    // Try to read FTBU centralized playtime (leaderboard/time_played) via reflection.
    // Returns ticks or null if unavailable.
    private Long getFTBUPlayTicks() {
        // Initialize FTBU reflection method handles on demand.
        // If an earlier probe failed but the mod is present, retry periodically.
        long now = net.minecraft.client.Minecraft.getSystemTime();
        boolean shouldProbe = false;
        if (!ftbuInitTried) {
            shouldProbe = true;
            ftbuInitTried = true;
            ftbuLastInitAttemptMs = now;
        } else if (com.thevoxelbox.yause.VoxelMenu.ftbUtilitiesInstalled && ftbuUniverseClass == null && ftbuForgePlayerClass == null && now - ftbuLastInitAttemptMs >= FTBU_INIT_RETRY_MS) {
            // previously failed but FTBU is installed — try again every FTBU_INIT_RETRY_MS
            shouldProbe = true;
            ftbuLastInitAttemptMs = now;
        }

        if (shouldProbe) {
            try {
                    ftbuUniverseClass = Class.forName("com.feed_the_beast.ftblib.lib.data.Universe");
                ftbuUniverseGet = ftbuUniverseClass.getMethod("get");
                // getPlayer overload accepts EntityPlayer, UUID or CharSequence
                for (java.lang.reflect.Method m : ftbuUniverseClass.getMethods()) {
                    if (m.getName().equals("getPlayer") && m.getParameterCount() == 1) {
                        Class<?> p = m.getParameterTypes()[0];
                        if (p.isAssignableFrom(this.mc.player.getClass()) || p == java.util.UUID.class || p == java.lang.CharSequence.class) {
                            ftbuUniverseGetPlayer = m;
                            break;
                        }
                    }
                }

                ftbuForgePlayerClass = Class.forName("com.feed_the_beast.ftblib.lib.data.ForgePlayer");
                ftbuStatsMethod = ftbuForgePlayerClass.getMethod("stats");
                Class<?> statsCls = ftbuStatsMethod.getReturnType();
                ftbuReadStatMethod = statsCls.getMethod("readStat", net.minecraft.stats.StatBase.class);
                
                // If Universe path wasn't fully present, try a fallback: some FTBLib versions expose
                // a static ForgePlayer getter directly on the ForgePlayer class (see initGui earlier).
                if ((ftbuUniverseClass == null || ftbuUniverseGet == null || ftbuUniverseGetPlayer == null) && ftbuForgePlayerClass == null) {
                    try {
                        Class<?> fp = Class.forName("com.feed_the_beast.ftblib.lib.data.ForgePlayer");
                        for (java.lang.reflect.Method m : fp.getMethods()) {
                            if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1) {
                                Class<?> p = m.getParameterTypes()[0];
                                if (p.isAssignableFrom(this.mc.player.getClass()) || p == java.util.UUID.class || p == java.lang.CharSequence.class) {
                                    ftbuForgePlayerStaticGetter = m;
                                    ftbuForgePlayerClass = fp;
                                    break;
                                }
                            }
                        }

                        if (ftbuForgePlayerClass != null && ftbuStatsMethod == null) {
                            ftbuStatsMethod = ftbuForgePlayerClass.getMethod("stats");
                            Class<?> statsCls2 = ftbuStatsMethod.getReturnType();
                            ftbuReadStatMethod = statsCls2.getMethod("readStat", net.minecraft.stats.StatBase.class);
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable t) {
                // ignore - optional integration
            }
            // If the probe succeeded (we now have at least one method path) clear the
            // 'unavailable' marker so subsequent reads will attempt to fetch stats.
            if (ftbuUniverseClass != null || ftbuForgePlayerClass != null || ftbuForgePlayerStaticGetter != null) {
                if (ftbuUnavailableLogged) {
                    ftbuUnavailableLogged = false;
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.info("FTBU reflection probe succeeded on retry — re-enabling FTBU playtime reads");
                }
            }
        }
            if (ftbuUniverseClass == null && ftbuForgePlayerClass == null) {
                // FTBU method pieces are missing — nothing we can do
                if (!ftbuUnavailableLogged) {
                    ftbuUnavailableLogged = true;
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.info("FTBU reflection not available — playtime via FTBU will be disabled (universeClass={}, universeGet={}, universeGetPlayer={}, forgePlayerClass={}, forgePlayerStaticGetter={})",
                            ftbuUniverseClass == null ? "<null>" : ftbuUniverseClass.getName(),
                            ftbuUniverseGet == null ? "<null>" : ftbuUniverseGet.getName(),
                            ftbuUniverseGetPlayer == null ? "<null>" : ftbuUniverseGetPlayer.getName(),
                            ftbuForgePlayerClass == null ? "<null>" : ftbuForgePlayerClass.getName(),
                            ftbuForgePlayerStaticGetter == null ? "<null>" : ftbuForgePlayerStaticGetter.getName());
                }
                return null;
            }

        try {
            Object forgePlayer = null;

            // Prefer Universe.get().getPlayer(...) if available
            if (ftbuUniverseGet != null && ftbuUniverseGetPlayer != null) {
                Object universe = ftbuUniverseGet.invoke(null);
                if (universe == null) return null;
                Class<?> param = ftbuUniverseGetPlayer.getParameterTypes()[0];
                if (param.isAssignableFrom(this.mc.player.getClass())) {
                    forgePlayer = ftbuUniverseGetPlayer.invoke(universe, this.mc.player);
                } else if (param == java.util.UUID.class) {
                    forgePlayer = ftbuUniverseGetPlayer.invoke(universe, this.mc.player.getUniqueID());
                } else if (param == java.lang.CharSequence.class) {
                    forgePlayer = ftbuUniverseGetPlayer.invoke(universe, this.mc.player.getName());
                }
            }

            // Fallback: some versions expose a static ForgePlayer getter on ForgePlayer class
            if (forgePlayer == null && ftbuForgePlayerStaticGetter != null) {
                Class<?> param = ftbuForgePlayerStaticGetter.getParameterTypes()[0];
                if (param.isAssignableFrom(this.mc.player.getClass())) {
                    forgePlayer = ftbuForgePlayerStaticGetter.invoke(null, this.mc.player);
                } else if (param == java.util.UUID.class) {
                    forgePlayer = ftbuForgePlayerStaticGetter.invoke(null, this.mc.player.getUniqueID());
                } else if (param == java.lang.CharSequence.class) {
                    forgePlayer = ftbuForgePlayerStaticGetter.invoke(null, this.mc.player.getName());
                }
            }

            if (forgePlayer == null) {
                // Log detailed probe state when forgePlayer can't be resolved
                com.thevoxelbox.yause.VoxelMenu.LOGGER.info("FTBU: forgePlayer lookup returned null (universe={}, getPlayerMethod={}, staticGetter={})",
                        ftbuUniverseClass == null ? "<null>" : ftbuUniverseClass.getName(),
                        ftbuUniverseGetPlayer == null ? "<null>" : ftbuUniverseGetPlayer == null ? "<null>" : ftbuUniverseGetPlayer.getName(),
                        ftbuForgePlayerStaticGetter == null ? "<null>" : ftbuForgePlayerStaticGetter.getName());
                return null;
            }

            Object stats = ftbuStatsMethod.invoke(forgePlayer);

            // Defensive: avoid directly invoking a potentially-null method handle
            Object val = null;
            java.lang.reflect.Method localRead = ftbuReadStatMethod;
            if (localRead != null) {
                try {
                    val = localRead.invoke(stats, StatList.PLAY_ONE_MINUTE);
                } catch (Throwable t2) {
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU: readStat invocation failed — will search for alternative methods: {}", t2.getMessage());
                }
            } else {
                com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU: no direct readStat method found on stats object (probe did not detect ftbuReadStatMethod) — attempting tolerant fallbacks");
            }

            // If the direct read failed or wasn't available, perform tolerant fallbacks on the stats object
            if (val == null && stats != null) {
                Class<?> statsClsAlt = stats.getClass();
                for (java.lang.reflect.Method m : statsClsAlt.getMethods()) {
                    String name = m.getName().toLowerCase();
                    if ((name.contains("read") || name.contains("get") || name.contains("value") || name.contains("stat")) && m.getParameterCount() == 1) {
                        Class<?> p = m.getParameterTypes()[0];
                        Object arg = null;
                        try {
                            if (p.isAssignableFrom(net.minecraft.stats.StatBase.class)) {
                                arg = StatList.PLAY_ONE_MINUTE;
                            } else if (p == String.class || p == java.lang.CharSequence.class) {
                                arg = StatList.PLAY_ONE_MINUTE.toString();
                            } else if (p == Integer.TYPE || p == Integer.class || p == Long.TYPE || p == Long.class) {
                                // Some stat implementations accept a numeric id — we never use vanilla stats,
                                // so we skip numeric-friendly candidate methods rather than seeding them
                                // with the vanilla player's stat value.
                                arg = null;
                            }

                            if (arg != null) {
                                Object rv = m.invoke(stats, arg);
                                if (rv != null) {
                                    val = rv;
                                    com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU: alternate stat-read method '{}' succeeded — value class={}", m.getName(), rv.getClass().getName());
                                    break;
                                }
                            }
                        } catch (Throwable ignore) { /* try next candidate */ }
                    }
                }
            }

            if (val == null) {
                // If stats object is null or val is null, log more details so we can diagnose missing method names
                if (stats == null) {
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.info("FTBU: stats object is null (forgePlayerClass={}, statsMethod={})",
                            ftbuForgePlayerClass == null ? "<null>" : ftbuForgePlayerClass.getName(),
                            ftbuStatsMethod == null ? "<null>" : ftbuStatsMethod.getName());
                } else {
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.info("FTBU: stats object present but stat read returned null (statsClass={})", stats.getClass().getName());
                }
                // If we can't read a playtime value, dump the probe info once — this helps diagnose "works in vanilla but not with mods" cases
                if (!ftbuProbeDumpLogged) {
                    ftbuProbeDumpLogged = true;
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("FTBU Probe Dump: \n");
                        sb.append("Universe class: ").append(ftbuUniverseClass == null ? "<null>" : ftbuUniverseClass.getName()).append('\n');
                        sb.append("Universe.get method: ").append(ftbuUniverseGet == null ? "<null>" : ftbuUniverseGet.getName()).append('\n');
                        sb.append("Universe.getPlayer method: ").append(ftbuUniverseGetPlayer == null ? "<null>" : ftbuUniverseGetPlayer.getName()).append('\n');
                        sb.append("ForgePlayer class: ").append(ftbuForgePlayerClass == null ? "<null>" : ftbuForgePlayerClass.getName()).append('\n');
                        sb.append("ForgePlayer.stats method: ").append(ftbuStatsMethod == null ? "<null>" : ftbuStatsMethod.getName()).append('\n');
                        sb.append("Stats-reader method: ").append(ftbuReadStatMethod == null ? "<null>" : ftbuReadStatMethod.getName()).append('\n');
                        // If we have a stats object, list its methods (shortened)
                        if (stats != null) {
                            sb.append("stats object class: ").append(stats.getClass().getName()).append('\n');
                            sb.append("stats methods:\n");
                            for (java.lang.reflect.Method m : stats.getClass().getMethods()) {
                                sb.append(" - ").append(m.getName()).append('(');
                                Class<?>[] pts = m.getParameterTypes();
                                for (int i=0;i<pts.length;i++) {
                                    if (i>0) sb.append(',');
                                    sb.append(pts[i].getSimpleName());
                                }
                                sb.append(')').append('\n');
                            }

                            sb.append("stats candidate reader methods:\n");
                            for (java.lang.reflect.Method m : stats.getClass().getMethods()) {
                                String n = m.getName().toLowerCase();
                                if ((n.contains("read") || n.contains("get") || n.contains("value") || n.contains("stat")) && m.getParameterCount() == 1) {
                                    sb.append(" - ").append(m.getName()).append('(').append(m.getParameterTypes()[0].getSimpleName()).append(")\n");
                                }
                            }
                        }
                        com.thevoxelbox.yause.VoxelMenu.LOGGER.warn(sb.toString());
                    } catch (Throwable ignored) {}
                }
                // No stat available for this player yet — mark missing so we don't spam logs.
                ftbuPlaytimeMissingLogged = true;
                com.thevoxelbox.yause.VoxelMenu.LOGGER.info("Yause: FTBU playtime not available in menu (cachedFTBUPlayTicks == null and basePlayTicks<0). ftbUtilitiesInstalled={}, ftbuInitTried={}, ftbuUnavailableLogged={}", com.thevoxelbox.yause.VoxelMenu.ftbUtilitiesInstalled, ftbuInitTried, ftbuUnavailableLogged);
                com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU: playtime stat read returned null (stats object: {}). Not falling back to vanilla stats.", stats == null ? "null" : stats.getClass().getName());
                return null;
            }
                // We got a value — ensure missing flag is cleared so the menu will display playtime.
                if (ftbuPlaytimeMissingLogged) {
                    ftbuPlaytimeMissingLogged = false;
                    com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU: playtime stat now available — clearing missing flag");
                }
                com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU: playtime stat value read: {} (class={})", val, val == null ? "null" : val.getClass().getName());
            if (val instanceof Number) return ((Number) val).longValue();
            return Long.parseLong(val.toString());
        } catch (Throwable t) {
            if (!ftbuUnavailableLogged) {
                ftbuUnavailableLogged = true;
                com.thevoxelbox.yause.VoxelMenu.LOGGER.debug("FTBU playtime reflection failed — disabling FTBU playtime ({}).", t.getMessage());
            }
            // We do not fall back to vanilla playtime when FTBU reflection fails.
            // Return null so callers know FTBU isn't available and the UI can omit a value.
            return null;
        }
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        // Adjust mouse x for the open animation translate so clicks map to drawn positions
        int boxX = com.thevoxelbox.yause.config.VoxelMenuConfig.menuOffsetX;
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
        int boxX = com.thevoxelbox.yause.config.VoxelMenuConfig.menuOffsetX;
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
            if (com.thevoxelbox.yause.config.VoxelMenuConfig.disableTransitions) {
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
        this.cachedFTBUPlayTicks = null;
        this.lastFTBRefreshMs = 0L;
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ++this.updateCounter;
        // increment session tick counter while pause menu is open so playtime updates in-real-time
        // Only update sessionPlayTicks when the game is actually running (not paused). The
        // pause menu should not artificially increment the player's playtime while the
        // integrated server/world is paused (single-player pause), so check isGamePaused.
        if (this.mc.player != null && this.openStartTimeMs >= 0 && !this.isClosing) {
            if (!this.mc.isGamePaused()) {
                ++this.sessionPlayTicks;
            }
        }

        // Periodic refresh of FTBU play ticks and FTB-Quests cache while the menu is open (reduce reflection frequency)
        if (this.openStartTimeMs >= 0 && !this.isClosing) {
            long now = net.minecraft.client.Minecraft.getSystemTime();
            if (now - this.lastFTBRefreshMs >= FTB_REFRESH_INTERVAL_MS) {
                this.lastFTBRefreshMs = now;
                if (VoxelMenuConfig.showPlaytime && com.thevoxelbox.yause.VoxelMenu.ftbUtilitiesInstalled) {
                    refreshFTBUPlayTicks();
                }
                if (VoxelMenuConfig.enableQuests && com.thevoxelbox.yause.VoxelMenu.ftbQuestsInstalled) {
                    updateFTBCache();
                }
            }
        }
    }
}



















