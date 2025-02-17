/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AutoCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay> displaySupplier;
    private String extraTooltip;
    private String errorTooltip;
    private List<Widget> setupDisplay;
    private AbstractContainerScreen<?> containerScreen;
    private boolean visible = false;
    private RecipeCategory<?> category;
    private Rectangle displayBounds;
    
    public AutoCraftingButtonWidget(Rectangle displayBounds, Rectangle rectangle, String text, Supplier<RecipeDisplay> displaySupplier, List<Widget> setupDisplay, RecipeCategory<?> recipeCategory) {
        super(rectangle, text);
        this.displayBounds = displayBounds;
        this.displaySupplier = () -> displaySupplier.get();
        Optional<Identifier> recipe = displaySupplier.get().getRecipeLocation();
        extraTooltip = recipe.isPresent() ? I18n.translate("text.rei.recipe_id", Formatting.GRAY.toString(), recipe.get().toString()) : "";
        this.containerScreen = ScreenHelper.getLastContainerScreen();
        this.setupDisplay = setupDisplay;
        this.category = recipeCategory;
    }
    
    @Override
    public void onPressed() {
        AutoTransferHandler.Context context = AutoTransferHandler.Context.create(true, containerScreen, displaySupplier.get());
        for (AutoTransferHandler autoTransferHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler())
            try {
                AutoTransferHandler.Result result = autoTransferHandler.handle(context);
                if (result.isSuccessful())
                    return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        minecraft.openScreen(containerScreen);
        ScreenHelper.getLastOverlay().init();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.enabled = false;
        String error = null;
        int color = 0;
        visible = false;
        IntList redSlots = null;
        AutoTransferHandler.Context context = AutoTransferHandler.Context.create(false, containerScreen, displaySupplier.get());
        for (AutoTransferHandler autoTransferHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler()) {
            try {
                AutoTransferHandler.Result result = autoTransferHandler.handle(context);
                if (result.isApplicable())
                    visible = true;
                if (result.isSuccessful()) {
                    enabled = true;
                    error = null;
                    break;
                } else if (error == null) {
                    error = result.getErrorKey();
                    color = result.getColor();
                    redSlots = result.getIntegers();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!visible) {
            enabled = false;
            error = "error.rei.no.handlers.applicable";
        }
        if (isHovered(mouseX, mouseY) && category instanceof TransferRecipeCategory && redSlots != null) {
            ((TransferRecipeCategory<RecipeDisplay>) category).renderRedSlots(setupDisplay, displayBounds, displaySupplier.get(), redSlots);
        }
        errorTooltip = error;
        int x = getBounds().x, y = getBounds().y, width = getBounds().width, height = getBounds().height;
        minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? BUTTON_LOCATION_DARK : BUTTON_LOCATION);
        RenderHelper.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int textureOffset = this.getTextureId(isHovered(mouseX, mouseY));
        RenderHelper.enableBlend();
        RenderHelper.blendFuncSeparate(770, 771, 1, 0);
        RenderHelper.blendFunc(770, 771);
        //Four Corners
        blit(x, y, 0, textureOffset * 80, 4, 4);
        blit(x + width - 4, y, 252, textureOffset * 80, 4, 4);
        blit(x, y + height - 4, 0, textureOffset * 80 + 76, 4, 4);
        blit(x + width - 4, y + height - 4, 252, textureOffset * 80 + 76, 4, 4);
        
        //Sides
        blit(x + 4, y, 4, textureOffset * 80, MathHelper.ceil((width - 8) / 2f), 4);
        blit(x + 4, y + height - 4, 4, textureOffset * 80 + 76, MathHelper.ceil((width - 8) / 2f), 4);
        blit(x + 4 + MathHelper.ceil((width - 8) / 2f), y + height - 4, 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80 + 76, MathHelper.floor((width - 8) / 2f), 4);
        blit(x + 4 + MathHelper.ceil((width - 8) / 2f), y, 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80, MathHelper.floor((width - 8) / 2f), 4);
        for (int i = y + 4; i < y + height - 4; i += 76) {
            blit(x, i, 0, 4 + textureOffset * 80, MathHelper.ceil(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76));
            blit(x + MathHelper.ceil(width / 2f), i, 256 - MathHelper.floor(width / 2f), 4 + textureOffset * 80, MathHelper.floor(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76));
        }
        
        int colour = 14737632;
        if (!this.visible) {
            colour = 10526880;
        } else if (enabled && isHovered(mouseX, mouseY)) {
            colour = 16777120;
        }
        
        fillGradient(x, y, x + width, y + height, color, color);
        this.drawCenteredString(font, text, x + width / 2, y + (height - 8) / 2, colour);
        
        if (getTooltips().isPresent())
            if (!focused && containsMouse(mouseX, mouseY))
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
            else if (focused)
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(new Point(x + width / 2, y + height / 2), getTooltips().get().split("\n")));
    }
    
    @Override
    protected int getTextureId(boolean boolean_1) {
        return !visible ? 0 : boolean_1 && enabled ? 2 : 1;
    }
    
    @Override
    public Optional<String> getTooltips() {
        if (this.minecraft.options.advancedItemTooltips)
            if (errorTooltip == null)
                return Optional.ofNullable(I18n.translate("text.auto_craft.move_items") + extraTooltip);
            else
                return Optional.ofNullable(Formatting.RED.toString() + I18n.translate(errorTooltip) + extraTooltip);
        if (errorTooltip == null)
            return Optional.ofNullable(I18n.translate("text.auto_craft.move_items"));
        else
            return Optional.ofNullable(Formatting.RED.toString() + I18n.translate(errorTooltip));
    }
}
