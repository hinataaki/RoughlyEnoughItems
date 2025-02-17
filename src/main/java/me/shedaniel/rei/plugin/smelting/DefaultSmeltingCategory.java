/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.smelting;

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.gui.renderers.RecipeRenderer;
import me.shedaniel.rei.gui.widget.RecipeArrowWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultSmeltingCategory implements TransferRecipeCategory<DefaultSmeltingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.SMELTING;
    }
    
    @Override
    public Renderer getIcon() {
        return Renderer.fromItemStack(new ItemStack(Blocks.FURNACE));
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.smelting");
    }
    
    @Override
    public RecipeRenderer getSimpleRenderer(DefaultSmeltingDisplay recipe) {
        return Renderer.fromRecipe(() -> Arrays.asList(recipe.getInput().get(0)), recipe::getOutput);
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultSmeltingDisplay> recipeDisplaySupplier, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 27);
        List<Widget> widgets = new LinkedList<>(Arrays.asList(new RecipeBaseWidget(bounds) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                RenderHelper.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                blit(startPoint.x, startPoint.y, 0, 54, 82, 54);
                int height = MathHelper.ceil((System.currentTimeMillis() / 250 % 14d) / 1f);
                blit(startPoint.x + 2, startPoint.y + 21 + (14 - height), 82, 77 + (14 - height), 14, height);
            }
        }));
        widgets.add(new RecipeArrowWidget(startPoint.x + 24, startPoint.y + 18, true));
        List<List<ItemStack>> input = recipeDisplaySupplier.get().getInput();
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 1, Renderer.fromItemStacks(input.get(0)), true, true, true));
        widgets.add(new SlotWidget(startPoint.x + 1, startPoint.y + 37, Renderer.fromItemStacks(() -> recipeDisplaySupplier.get().getFuel(), true, stack -> Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.smelting.fuel"))), true, true, true));
        widgets.add(new SlotWidget(startPoint.x + 61, startPoint.y + 19, Renderer.fromItemStacks(recipeDisplaySupplier.get().getOutput()), false, true, true));
        return widgets;
    }
    
    @Override
    public void renderRedSlots(List<Widget> widgets, Rectangle bounds, DefaultSmeltingDisplay display, IntList redSlots) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 27);
        RenderHelper.translatef(0, 0, 400);
        if (redSlots.contains(0)) {
            DrawableHelper.fill(startPoint.x + 1, startPoint.y + 1, startPoint.x + 1 + 16, startPoint.y + 1 + 16, 822018048);
        }
        RenderHelper.translatef(0, 0, -400);
    }
    
}
