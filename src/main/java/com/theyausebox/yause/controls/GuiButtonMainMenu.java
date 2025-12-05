package com.theyausebox.yause.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class GuiButtonMainMenu extends GuiButton {
    public boolean rightAlign;
    public int textWidth;

    public boolean highlightEnabled = true;

    private long lastUpdateTimeMs = -1L;
    private float alphaFalloffRate = 0.12f;
    protected float alpha = 0.0f;
    public int yOffset = 0;

    public GuiButtonMainMenu(int buttonId, String displayString) {
        this(buttonId, 0, 0, displayString, false);
    }

    public GuiButtonMainMenu(int buttonId, int xPosition, int yPosition, String displayString, boolean rightAlign) {
        super(buttonId, xPosition, yPosition, displayString);
        this.height = 16;
        this.width = 150;
        this.textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString);
        this.rightAlign = rightAlign;

        try {
            this.alphaFalloffRate = (float) com.theyausebox.yause.config.YauseMenuConfig.hoverAlphaFalloffRate;
        } catch (Exception ignored) {

        }
    }

    public void updateButton(int updateCounter, float partialTicks, int mouseX, int mouseY) {
        if (this.visible) {

            try {
                this.alphaFalloffRate = (float) com.theyausebox.yause.config.YauseMenuConfig.hoverAlphaFalloffRate;
            } catch (Throwable ignored) {}

            long now = Minecraft.getSystemTime();
            if (this.lastUpdateTimeMs < 0L) {

                this.lastUpdateTimeMs = now - 50L;
            }
            long deltaMs = now - this.lastUpdateTimeMs;

            if (deltaMs <= 0L) {
                deltaMs = 0L;
            } else if (deltaMs > 250L) {
                deltaMs = 250L;
            }
            this.lastUpdateTimeMs = now;
            float deltaTime = (float)deltaMs / 50.0f;
            boolean mouseOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            if (mouseOver && this.highlightEnabled) {

                float targetAlpha = 0.4f;
                float change = this.alphaFalloffRate * deltaTime;
                if (this.alpha < targetAlpha) {
                    this.alpha = Math.min(targetAlpha, this.alpha + change);
                } else if (this.alpha > targetAlpha) {
                    this.alpha = Math.max(targetAlpha, this.alpha - change);
                }
            } else if (this.alpha > 0.0f) {

                this.alpha = Math.max(0.0f, this.alpha - this.alphaFalloffRate * deltaTime);
            }
            if (this.alpha < 0.0f) {
                this.alpha = 0.0f;
            }
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            boolean mouseOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            if (this.alpha > 0.0f) {
                float fade = com.theyausebox.yause.GuiIngameMenuYauseBox.currentOpenProgress;

                float effectiveAlpha = mouseOver ? 0.4f : this.alpha;
                int baseAlphaVal = Math.round(255.0f * effectiveAlpha) & 0xFF;
                int hlAlpha = (Math.round(baseAlphaVal * fade) & 0xFF) << 24;

                int hlGBCol = 0;
                this.drawButtonBackground(mouseOver, hlAlpha, hlGBCol);
            }
            this.mouseDragged(mc, mouseX, mouseY);
            this.drawButtonText(mouseOver, mc);
        }
    }

    public void drawButtonText(boolean mouseOver, Minecraft mc) {
        float fade = com.theyausebox.yause.GuiIngameMenuYauseBox.currentOpenProgress;
        int textColour = 0xFFFFFF;
        if (!this.enabled) {
            textColour = 0xA0A0A0;
        } else if (mouseOver) {
            textColour = 0x50FFFF;
        }
        int BUTTON_SHIFT = 5;
        int textIndent = 4 + Math.round(10.0f * this.alpha) + BUTTON_SHIFT;
        int textAlpha = Math.round(255 * fade);
        textColour = (textAlpha << 24) | (textColour & 0xFFFFFF);
        int rightIndentExtra = 8;
        if (this.rightAlign) {

            this.drawString(mc.fontRenderer, this.displayString, this.x + this.width - (textIndent + rightIndentExtra) - this.textWidth, this.y + (this.height - 8) / 2, textColour);
        } else {
            this.drawString(mc.fontRenderer, this.displayString, this.x + textIndent, this.y + (this.height - 8) / 2, textColour);
        }
    }

    public void drawButtonBackground(boolean mouseOver, int hlAlpha, int hlGBCol) {

        int w = (int) ((float)this.width * Math.min(1.0f, 2.5f * this.alpha));
        if (this.rightAlign) {
            drawRect(this.x + this.width - w, this.y, this.x + this.width, this.y + this.height, hlAlpha | hlGBCol << 8 | hlGBCol, 4);
        } else {
            drawRect(this.x, this.y, this.x + w, this.y + this.height, hlAlpha | hlGBCol << 8 | hlGBCol, 4);
        }
    }

    public static void drawRect(int x1, int y1, int x2, int y2, int colour, int offset) {
        int var5;
        if (x1 < x2) {
            var5 = x1;
            x1 = x2;
            x2 = var5;
        }
        if (y1 < y2) {
            var5 = y1;
            y1 = y2;
            y2 = var5;
        }
        float var10 = (float)(colour >> 24 & 0xFF) / 255.0f;
        float var6 = (float)(colour >> 16 & 0xFF) / 255.0f;
        float var7 = (float)(colour >> 8 & 0xFF) / 255.0f;
        float var8 = (float)(colour & 0xFF) / 255.0f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(var6, var7, var8, var10);
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos((double)x1 + (double)offset, (double)y2, 0.0).endVertex();
        bufferBuilder.pos((double)x2, (double)y2, 0.0).endVertex();
        bufferBuilder.pos((double)x2 - (double)offset, (double)y1, 0.0).endVertex();
        bufferBuilder.pos((double)x1, (double)y1, 0.0).endVertex();
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public int getWidth() {
        return this.width;
    }
}
