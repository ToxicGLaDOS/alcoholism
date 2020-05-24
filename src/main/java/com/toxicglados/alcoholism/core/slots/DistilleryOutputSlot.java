package com.toxicglados.alcoholism.core.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class DistilleryOutputSlot extends AlcoholismSlot {
    public DistilleryOutputSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        // Return false because no item is valid for an output slot
        return false;
    }
}
