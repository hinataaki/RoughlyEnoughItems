/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.server;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InputSlotCrafter<C extends Inventory> implements RecipeGridAligner<Integer> {
    
    protected Container craftingContainer;
    protected ContainerInfo containerInfo;
    protected PlayerInventory inventory;
    
    private InputSlotCrafter(Container craftingContainer, ContainerInfo containerInfo) {
        this.craftingContainer = craftingContainer;
        this.containerInfo = containerInfo;
    }
    
    public static <C extends Inventory> void start(Identifier category, Container craftingContainer_1, ServerPlayerEntity player, Map<Integer, List<ItemStack>> map, boolean hasShift) {
        ContainerInfo containerInfo = ContainerInfoHandler.getContainerInfo(category, craftingContainer_1.getClass());
        new InputSlotCrafter<C>(craftingContainer_1, containerInfo).fillInputSlots(player, map, hasShift);
    }
    
    private void fillInputSlots(ServerPlayerEntity player, Map<Integer, List<ItemStack>> map, boolean hasShift) {
        this.inventory = player.inventory;
        if (this.canReturnInputs() || player.isCreative()) {
            // Return the already placed items on the grid
            this.returnInputs();
            
            RecipeFinder recipeFinder = new RecipeFinder();
            recipeFinder.clear();
            for (ItemStack stack : player.inventory.main) {
                recipeFinder.addNormalItem(stack);
            }
            this.containerInfo.populateRecipeFinder(craftingContainer, recipeFinder);
            DefaultedList<Ingredient> ingredients = DefaultedList.of();
            map.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(entry -> {
                ingredients.add(Ingredient.ofStacks(entry.getValue().toArray(new ItemStack[0])));
            });
            if (recipeFinder.findRecipe(ingredients, (IntList) null)) {
                this.fillInputSlots(recipeFinder, ingredients, hasShift);
            } else {
                this.returnInputs();
                craftingContainer.sendContentUpdates();
                player.inventory.markDirty();
                throw new NullPointerException();
            }
            
            craftingContainer.sendContentUpdates();
            player.inventory.markDirty();
        }
    }
    
    @Override
    public void acceptAlignedInput(Iterator<Integer> iterator_1, int int_1, int int_2, int int_3, int int_4) {
        Slot slot_1 = this.craftingContainer.getSlot(int_1);
        ItemStack itemStack_1 = net.minecraft.recipe.RecipeFinder.getStackFromId((Integer) iterator_1.next());
        if (!itemStack_1.isEmpty()) {
            for (int int_5 = 0; int_5 < int_2; ++int_5) {
                this.fillInputSlot(slot_1, itemStack_1);
            }
        }
    }
    
    protected void fillInputSlot(Slot slot_1, ItemStack itemStack_1) {
        int int_1 = this.inventory.method_7371(itemStack_1);
        if (int_1 != -1) {
            ItemStack itemStack_2 = this.inventory.getInvStack(int_1).copy();
            if (!itemStack_2.isEmpty()) {
                if (itemStack_2.getCount() > 1) {
                    this.inventory.takeInvStack(int_1, 1);
                } else {
                    this.inventory.removeInvStack(int_1);
                }
                
                itemStack_2.setCount(1);
                if (slot_1.getStack().isEmpty()) {
                    slot_1.setStack(itemStack_2);
                } else {
                    slot_1.getStack().increment(1);
                }
                
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    protected void fillInputSlots(RecipeFinder recipeFinder, DefaultedList<Ingredient> ingredients, boolean hasShift) {
        //        boolean boolean_2 = this.craftingContainer.matches(recipe_1);
        boolean boolean_2 = false;
        int int_1 = recipeFinder.countRecipeCrafts(ingredients, (IntList) null);
        int int_2;
        if (boolean_2) {
            for (int_2 = 0; int_2 < this.containerInfo.getCraftingHeight(craftingContainer) * this.containerInfo.getCraftingWidth(craftingContainer) + 1; ++int_2) {
                if (int_2 != this.containerInfo.getCraftingResultSlotIndex(craftingContainer)) {
                    ItemStack itemStack_1 = this.craftingContainer.getSlot(int_2).getStack();
                    if (!itemStack_1.isEmpty() && Math.min(int_1, itemStack_1.getMaxCount()) < itemStack_1.getCount() + 1) {
                        return;
                    }
                }
            }
        }
        
        int_2 = this.getAmountToFill(hasShift, int_1, boolean_2);
        IntList intList_1 = new IntArrayList();
        if (recipeFinder.findRecipe(ingredients, intList_1, int_2)) {
            int int_4 = int_2;
            IntListIterator var8 = intList_1.iterator();
            
            while (var8.hasNext()) {
                int int_5 = (Integer) var8.next();
                int int_6 = RecipeFinder.getStackFromId(int_5).getMaxCount();
                if (int_6 < int_4) {
                    int_4 = int_6;
                }
            }
            
            if (recipeFinder.findRecipe(ingredients, intList_1, int_4)) {
                this.returnInputs();
                this.alignRecipeToGrid(this.containerInfo.getCraftingWidth(craftingContainer), this.containerInfo.getCraftingHeight(craftingContainer), this.containerInfo.getCraftingResultSlotIndex(craftingContainer), ingredients, intList_1.iterator(), int_4);
            }
        }
        
    }
    
    protected int getAmountToFill(boolean hasShift, int int_1, boolean boolean_2) {
        int int_2 = 1;
        if (hasShift) {
            int_2 = int_1;
        } else if (boolean_2) {
            int_2 = 64;
            for (int int_3 = 0; int_3 < this.containerInfo.getCraftingWidth(craftingContainer) * this.containerInfo.getCraftingHeight(craftingContainer) + 1; ++int_3) {
                if (int_3 != this.containerInfo.getCraftingResultSlotIndex(craftingContainer)) {
                    ItemStack itemStack_1 = this.craftingContainer.getSlot(int_3).getStack();
                    if (!itemStack_1.isEmpty() && int_2 > itemStack_1.getCount()) {
                        int_2 = itemStack_1.getCount();
                    }
                }
            }
            if (int_2 < 64) {
                ++int_2;
            }
        }
        return int_2;
    }
    
    protected void returnInputs() {
        for (int int_1 = 0; int_1 < this.containerInfo.getCraftingWidth(craftingContainer) * this.containerInfo.getCraftingHeight(craftingContainer) + 1; ++int_1) {
            if (int_1 != this.containerInfo.getCraftingResultSlotIndex(craftingContainer)) {
                this.returnSlot(int_1);
            }
        }
        
        this.containerInfo.clearCraftingSlots(craftingContainer);
    }
    
    protected void returnSlot(int int_1) {
        ItemStack itemStack_1 = this.craftingContainer.getSlot(int_1).getStack();
        if (!itemStack_1.isEmpty()) {
            for (; itemStack_1.getCount() > 0; this.craftingContainer.getSlot(int_1).takeStack(1)) {
                int int_2 = this.inventory.getOccupiedSlotWithRoomForStack(itemStack_1);
                if (int_2 == -1) {
                    int_2 = this.inventory.getEmptySlot();
                }
                
                ItemStack itemStack_2 = itemStack_1.copy();
                itemStack_2.setCount(1);
                if (!this.inventory.insertStack(int_2, itemStack_2)) {
                    throw new IllegalStateException("rei.rei.no.slot.in.inv");
                }
            }
        }
    }
    
    private boolean canReturnInputs() {
        List<ItemStack> list_1 = Lists.newArrayList();
        int int_1 = this.getFreeInventorySlots();
        
        for (int int_2 = 0; int_2 < this.containerInfo.getCraftingWidth(craftingContainer) * this.containerInfo.getCraftingHeight(craftingContainer) + 1; ++int_2) {
            if (int_2 != this.containerInfo.getCraftingResultSlotIndex(craftingContainer)) {
                ItemStack itemStack_1 = this.craftingContainer.getSlot(int_2).getStack().copy();
                if (!itemStack_1.isEmpty()) {
                    int int_3 = this.inventory.getOccupiedSlotWithRoomForStack(itemStack_1);
                    if (int_3 == -1 && list_1.size() <= int_1) {
                        Iterator var6 = list_1.iterator();
                        
                        while (var6.hasNext()) {
                            ItemStack itemStack_2 = (ItemStack) var6.next();
                            if (itemStack_2.isItemEqualIgnoreDamage(itemStack_1) && itemStack_2.getCount() != itemStack_2.getMaxCount() && itemStack_2.getCount() + itemStack_1.getCount() <= itemStack_2.getMaxCount()) {
                                itemStack_2.increment(itemStack_1.getCount());
                                itemStack_1.setCount(0);
                                break;
                            }
                        }
                        
                        if (!itemStack_1.isEmpty()) {
                            if (list_1.size() >= int_1) {
                                return false;
                            }
                            
                            list_1.add(itemStack_1);
                        }
                    } else if (int_3 == -1) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private int getFreeInventorySlots() {
        int int_1 = 0;
        Iterator var2 = this.inventory.main.iterator();
        while (var2.hasNext()) {
            ItemStack itemStack_1 = (ItemStack) var2.next();
            if (itemStack_1.isEmpty()) {
                ++int_1;
            }
        }
        return int_1;
    }
    
}
