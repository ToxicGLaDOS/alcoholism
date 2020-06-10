package com.toxicglados.alcoholism.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

// A class for containers with a single output slot
// The output slot MUST be the last slot added to the container
public abstract class SingleOutputContainer extends AlcoholismContainer {
    protected final int playerInvStartIndex;

    protected SingleOutputContainer(@Nullable ContainerType<?> type, int id, int playerInvStartIndex) {
        super(type, id);
        this.playerInvStartIndex = playerInvStartIndex;
    }


    // Handles the shift-click behaviour of slots for this container
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {

        ItemStack itemStack = ItemStack.EMPTY;
        Slot clickedSlot = this.inventorySlots.get(index);
        if(clickedSlot != null && clickedSlot.getHasStack()){
            ItemStack clickedItemStack = clickedSlot.getStack();
            itemStack = clickedItemStack.copy();
            // If we shift-click on one of the items in the container
            if(index < this.playerInvStartIndex){
                // If we can't merge the itemStack we clicked on with one in the player inventory
                if(!this.mergeItemStack(clickedItemStack, this.playerInvStartIndex, this.inventorySlots.size(), false)){
                    return ItemStack.EMPTY;
                }
            }
            // If we can't merge the itemStack with one in the container but not the output (hence the -1)
            else if (!this.mergeItemStack(clickedItemStack, 0, this.playerInvStartIndex - 1, false)){
                return ItemStack.EMPTY;
            }

            if(clickedItemStack.isEmpty()) {
                clickedSlot.putStack(ItemStack.EMPTY);
            }
            else{
                clickedSlot.onSlotChanged();
            }
        }
        return itemStack;
    }
}
