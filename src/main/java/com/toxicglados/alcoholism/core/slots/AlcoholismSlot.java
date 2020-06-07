package com.toxicglados.alcoholism.core.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class AlcoholismSlot extends Slot {
    public AlcoholismSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    // This is the maximum items in a stack for this slot
    @Override
    public int getItemStackLimit(ItemStack stack) {
        // This ends up being whatever the stack size for the item is
        // So 1 for buckets of lava and 64 for most blocks
        return super.getItemStackLimit(stack);
    }
}
