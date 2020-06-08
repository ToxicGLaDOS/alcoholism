package com.toxicglados.alcoholism.container;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.core.slots.DistilleryFuelSlot;
import com.toxicglados.alcoholism.core.slots.DistillerySlot;
import com.toxicglados.alcoholism.core.slots.OutputSlot;
import com.toxicglados.alcoholism.tileentity.DistilleryTileEntity;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Objects;

public class DistilleryContainer extends AlcoholismContainer {

    public final DistilleryTileEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;
    private final IIntArray distilleryData;
    private final int playerInvStartIndex;

    public DistilleryContainer(final int windowId, final PlayerInventory playerInventory, final DistilleryTileEntity tileEntity) {
        super(RegistryHandler.DISTILLERY_CONTAINER.get(), windowId);
        this.tileEntity = tileEntity;
        this.distilleryData = tileEntity.distilleryData;
        this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());

        // This is where the player inventory starts
        // the first 3 are the inventory of the container
        this.playerInvStartIndex = 3;

        int startX = 8;
        int slotSize = 14;
        int bufferSize = 4;

        int slotGapSize = slotSize + bufferSize;

        int ingredientInputX = 55;
        int ingredientInputY = 17;

        int fuelInputX = 55;
        int fuelInputY = 52;

        int outputX = 116;
        int outputY = 35;

        // THIS IS SUPER IMPORTANT
        // IT KEEPS THE distilleryData IN SYNC WITH CLIENT
        this.trackIntArray(tileEntity.distilleryData);

        // Ingredient slot
        this.addSlot(new DistillerySlot(tileEntity, 0, ingredientInputX, ingredientInputY));

        // Fuel slot
        this.addSlot(new DistilleryFuelSlot(tileEntity, 1, fuelInputX, fuelInputY));

        // Output slot
        this.addSlot(new OutputSlot(tileEntity, 2, outputX, outputY));


        // Main Player Inventory
        int playerIvnY = 84;
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 9; column++){
                // Add 9 to index because the inventory starts with the hotbar
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, startX + column * slotGapSize, playerIvnY + row * slotGapSize));
            }
        }

        // Player Hotbar
        int playerHotbarY = 142;

        for(int column = 0; column < 9; column++){
            this.addSlot(new Slot(playerInventory, column, startX + column * slotGapSize, playerHotbarY));
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

    @OnlyIn(Dist.CLIENT)
    public int getCookProgressionScaled(int scale) {
        int i = this.distilleryData.get(2);
        int j = this.distilleryData.get(3);
        return j != 0 && i != 0 ? i * scale / j : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBurnLeftScaled(int scale) {
        int i = this.distilleryData.get(1);
        if (i == 0) {
            i = 200;
        }

        return this.distilleryData.get(0) * scale / i;
    }


    public DistilleryTileEntity getTileEntity(){
        return tileEntity;
    }
}
