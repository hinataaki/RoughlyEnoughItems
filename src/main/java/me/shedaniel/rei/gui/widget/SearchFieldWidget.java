/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;

public class SearchFieldWidget extends TextFieldWidget {
    
    public static boolean isSearching = false;
    public long keybindFocusTime = -1;
    public int keybindFocusKey = -1;
    protected long lastClickedTime = -1;
    
    public SearchFieldWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        setMaxLength(10000);
    }
    
    public void laterRender(int int_1, int int_2, float float_1) {
        GuiLighting.disable();
        RenderHelper.disableDepthTest();
        setEditableColor(ContainerScreenOverlay.getEntryListWidget().children().isEmpty() && !getText().isEmpty() ? 16733525 : isSearching ? -1313241 : 14737632);
        setSuggestion(!isFocused() && getText().isEmpty() ? I18n.translate("text.rei.search.field.suggestion") : null);
        super.render(int_1, int_2, float_1);
        RenderHelper.enableDepthTest();
    }
    
    @Override
    public void renderBorder() {
        if (!isSearching)
            super.renderBorder();
        else if (this.hasBorder()) {
            fill(this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, -1313241);
            fill(this.getBounds().x, this.getBounds().y, this.getBounds().x + this.getBounds().width, this.getBounds().y + this.getBounds().height, -16777216);
        }
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        boolean contains = containsMouse(double_1, double_2);
        if (isVisible() && contains && int_1 == 1)
            setText("");
        if (contains && int_1 == 0)
            if (lastClickedTime == -1)
                lastClickedTime = System.currentTimeMillis();
            else if (System.currentTimeMillis() - lastClickedTime > 1200)
                lastClickedTime = -1;
            else {
                lastClickedTime = -1;
                isSearching = !isSearching;
                minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        return super.mouseClicked(double_1, double_2, int_1);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isVisible() && this.isFocused())
            if (int_1 == 257 || int_1 == 335) {
                setFocused(false);
                return true;
            }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (System.currentTimeMillis() - keybindFocusTime < 1000 && InputUtil.isKeyPressed(MinecraftClient.getInstance().window.getHandle(), keybindFocusKey)) {
            keybindFocusTime = -1;
            keybindFocusKey = -1;
            return true;
        }
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
    }
    
}
