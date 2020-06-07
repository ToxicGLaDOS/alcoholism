package com.toxicglados.alcoholism.core.slots;

import com.toxicglados.alcoholism.core.recipes.FermentingVatRecipes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class FermentingVatSlot extends AlcoholismSlot {

    public FermentingVatSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        // Basically, is there a valid recipe that uses this item
        return FermentingVatRecipes.instance().getCookingResult(stack) != ItemStack.EMPTY;
    }
}
