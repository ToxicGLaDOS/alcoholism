package com.toxicglados.alcoholism.core.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;

public class DistilleryFuelSlot extends AlcoholismSlot {
    public DistilleryFuelSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return isFuel(stack);
    }

    // This is the maximum items in a stack for this slot
    @Override
    public int getItemStackLimit(ItemStack stack) {
        // This ends up being whatever the stack size for the item is
        // So 1 for buckets of lava and 64 for most blocks
        return super.getItemStackLimit(stack);
    }

    protected boolean isFuel(ItemStack stack) {
        // TODO: Determine if this is the right way to do this.
        return AbstractFurnaceTileEntity.isFuel(stack);
    }
}
