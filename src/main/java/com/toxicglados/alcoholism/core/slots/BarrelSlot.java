package com.toxicglados.alcoholism.core.slots;

import com.toxicglados.alcoholism.core.recipes.BarrelRecipes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class BarrelSlot extends AlcoholismSlot {
    public BarrelSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return BarrelRecipes.instance().hasRecipeFor(stack);
    }
}
