package com.toxicglados.alcoholism.container;

import com.toxicglados.alcoholism.tileentity.DistilleryTileEntity;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;

import javax.annotation.Nullable;
import java.util.Objects;

public class DistilleryContainer extends Container {

    public final DistilleryTileEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;

    public DistilleryContainer(final int windowId, final PlayerInventory playerInventory, final DistilleryTileEntity tileEntity) {
        super(RegistryHandler.DISTILLERY_CONTAINER.get(), windowId);
        this.tileEntity = tileEntity;
        this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());

        // Main inventory
        int startX = 8;
        int startY = 18;
        int slotSizePlus2 = 18;

        for(int row = 0; row < 4; row++){
            for(int column = 0; column < 9; column++){
                this.addSlot(new Slot(tileEntity, (row * 9) + column, startX + column * slotSizePlus2, startY + row * slotSizePlus2));
            }
        }

        // Main Player Inventory
        int playerIvnY = startY * 5 + 12;
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 9; column++){
                // Add 9 to index because the inventory starts with the hotbar
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, startX + column * slotSizePlus2, playerIvnY + row * slotSizePlus2));
            }
        }

        // Player Hotbar
        int playerHotbarY = playerIvnY + (playerIvnY / 2) + 7;

        for(int column = 0; column < 9; column++){
            this.addSlot(new Slot(playerInventory, column, startX + column * slotSizePlus2, playerHotbarY));
        }
    }

    public DistilleryContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, RegistryHandler.DISTILLERY_BLOCK.get());
    }

    private static DistilleryTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");

        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());

        if(tileAtPos instanceof DistilleryTileEntity){
            return (DistilleryTileEntity)tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if(slot != null && slot.getHasStack()){
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();
            if(index < 36){
                if(!this.mergeItemStack(itemStack1, 36, this.inventorySlots.size(), true)){
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemStack1, 0, 36, false)){
                return ItemStack.EMPTY;
            }

            if(itemStack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            else{
                slot.onSlotChanged();
            }
        }
        return itemStack;
    }
}
