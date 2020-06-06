package com.toxicglados.alcoholism.core.slots;

import com.toxicglados.alcoholism.core.recipes.TahonaRecipes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;

public class TahonaSlot extends AlcoholismSlot {
    public TahonaSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        // Basically, is there a valid recipe that uses this item
        return TahonaRecipes.instance().getCookingResult(stack) != ItemStack.EMPTY;
    }

    // This is the maximum items in a stack for this slot
    @Override
    public int getItemStackLimit(ItemStack stack) {
        // This ends up being whatever the stack size for the item is
        // So 1 for buckets of lava and 64 for most blocks
        return super.getItemStackLimit(stack);
    }
}
