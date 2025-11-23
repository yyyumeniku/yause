package com.theyausebox.yause.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiButtonPanel extends GuiButton {
    protected AnchorType anchorType = AnchorType.BottomLeft;
    protected List<GuiButtonMainMenu> buttons = new ArrayList<GuiButtonMainMenu>();
    protected GuiButtonMainMenu pressedButton;
    protected int xOffset;
    protected int yOffset;
    protected int buttonSpacing = 16;
    protected int nextButtonId;
    protected String tag;

    public GuiButtonPanel(int buttonIdBase, AnchorType anchorType, int xOffset, int yOffset, int width, int height, int buttonSpacing, int containerWidth, int containerHeight, String tag) {
        super(buttonIdBase, 0, 0, width, height, null);
        this.anchorType = anchorType;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.buttonSpacing = buttonSpacing;
        this.nextButtonId = buttonIdBase + 1;
        this.tag = tag;
        this.updatePosition(containerWidth, containerHeight);
    }

    public GuiButtonMainMenu addButton(String displayText) {
        return this.addButton(displayText, this.nextButtonId++);
    }


    public GuiButtonMainMenu addButton(String displayText, int buttonId) {
        GuiButtonMainMenu button = new GuiButtonMainMenu(buttonId, displayText);
        this.buttons.add(button);
        this.updateButtonPositions();
        return button;
    }

    // Custom-screen feature and buttons were removed when the main-menu system
    // was deleted. Panels now only accept plain text buttons.

    public void removeButton(GuiButtonMainMenu button) {
        this.buttons.remove(button);
        this.updateButtonPositions();
    }

    public void updatePosition(int containerWidth, int containerHeight) {
        this.y = this.anchorType == AnchorType.TopRight || this.anchorType == AnchorType.TopLeft ? this.yOffset : containerHeight - this.height - this.yOffset;
        this.x = this.anchorType == AnchorType.TopLeft || this.anchorType == AnchorType.BottomLeft ? this.xOffset : containerWidth - this.width - this.xOffset;
        this.updateButtonPositions();
    }

    private void updateButtonPositions() {
        if (this.anchorType == AnchorType.TopLeft || this.anchorType == AnchorType.BottomLeft) {
            int buttonXPosition = 0;
            for (GuiButtonMainMenu button : this.buttons) {
                button.rightAlign = false;
                button.x = buttonXPosition;
            }
        } else {
            for (GuiButtonMainMenu button : this.buttons) {
                button.rightAlign = true;
                button.x = this.width - button.getWidth();
            }
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        mouseX -= this.x;
        mouseY -= this.y;
        int buttonYPosition = 0;
        int visibleButtonCount = 0;
        
        for (GuiButtonMainMenu button : this.buttons) {
            if (button.visible) {
                ++visibleButtonCount;
            }
        }
        
        if (this.anchorType == AnchorType.BottomLeft || this.anchorType == AnchorType.BottomRight) {
            buttonYPosition = this.height - this.buttonSpacing * visibleButtonCount;
        }
        
        GL11.glPushMatrix();
        GL11.glTranslatef((float)this.x, (float)this.y, 0.0f);
        for (GuiButtonMainMenu button : this.buttons) {
            if (button.visible) {
                button.y = buttonYPosition + button.yOffset;
                buttonYPosition += this.buttonSpacing;
            }
            button.drawButton(minecraft, mouseX, mouseY, partialTicks);
        }
        GL11.glPopMatrix();
    }

    public void updateButtons(int updateCounter, float partialTicks, int mouseX, int mouseY) {
        mouseX -= this.x;
        mouseY -= this.y;
        for (GuiButtonMainMenu button : this.buttons) {
            button.updateButton(updateCounter, partialTicks, mouseX, mouseY);
        }
    }

    public GuiButtonMainMenu getPressedButton() {
        return this.pressedButton;
    }

    public int getPressedButtonId() {
        return this.pressedButton != null ? this.pressedButton.id : -1;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        mouseX -= this.x;
        mouseY -= this.y;
        for (GuiButtonMainMenu button : this.buttons) {
            if (button.mousePressed(minecraft, mouseX, mouseY)) {
                this.pressedButton = button;
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (this.pressedButton != null) {
            this.pressedButton.mouseReleased(mouseX, mouseY);
            this.pressedButton = null;
        }
    }

    public boolean isMouseOver(GuiButtonMainMenu button, Minecraft minecraft, int mouseX, int mouseY) {
        if (!this.buttons.contains(button)) {
            return false;
        }
        return button.mousePressed(minecraft, mouseX -= this.x, mouseY -= this.y);
    }

    public int getAdjustedXPosition(GuiButtonMainMenu button) {
        return (!this.buttons.contains(button) ? 0 : button.x) + this.x;
    }

    public int getAdjustedYPosition(GuiButtonMainMenu button) {
        return (!this.buttons.contains(button) ? 0 : button.y) + this.y;
    }

    public String getTag() {
        return this.tag;
    }

    public boolean tagMatches(String tag) {
        return this.tag != null && this.tag.equalsIgnoreCase(tag);
    }

    // Public getters so external classes can query layout without directly accessing protected fields
    public int getPanelWidth() {
        return this.width;
    }

    public int getPanelHeight() {
        return this.height;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public enum AnchorType {
        TopLeft,
        BottomLeft,
        TopRight,
        BottomRight
    }
}
