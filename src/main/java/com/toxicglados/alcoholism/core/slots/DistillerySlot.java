package com.toxicglados.alcoholism.core.slots;

import com.toxicglados.alcoholism.core.recipes.DistilleryRecipes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;

public class DistillerySlot extends AlcoholismSlot{
    public DistillerySlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return DistilleryRecipes.instance().hasRecipeFor(stack);
    }

}
