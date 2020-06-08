package com.toxicglados.alcoholism.container;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.core.slots.OutputSlot;
import com.toxicglados.alcoholism.core.slots.TahonaSlot;
import com.toxicglados.alcoholism.tileentity.TahonaTileEntity;
import com.toxicglados.alcoholism.util.AlcoholismIntReferenceHolder;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.core.jmx.Server;
import sun.net.www.content.text.plain;

import java.util.Objects;

public class TahonaContainer extends AlcoholismContainer {

    public final TahonaTileEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;
    private final IIntArray tahonaData;
    private final int playerInvStartIndex;

    public TahonaContainer(final int windowId, final PlayerInventory playerInventory, final TahonaTileEntity tileEntity) {
        super(RegistryHandler.TAHONA_CONTAINER.get(), windowId);
        this.tileEntity = tileEntity;
        this.tahonaData = tileEntity.tahonaData;

        // This is where the player inventory starts
        // the first 2 are the inventory of the container
        this.playerInvStartIndex = 2;

        this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());
        int startX = 8;
        int slotSize = 14;
        int bufferSize = 4;

        int slotGapSize = slotSize + bufferSize;

        int ingredientInputX = 55;
        int ingredientInputY = 33;

        int outputX = 116;
        int outputY = 35;

        // Ingredient slot
        this.addSlot(new TahonaSlot(tileEntity, 0, ingredientInputX, ingredientInputY));

        // Output slot
        this.addSlot(new OutputSlot(tileEntity, 1, outputX, outputY));


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

        // THIS IS SUPER IMPORTANT
        // IT KEEPS THE tahonaData IN SYNC WITH CLIENT
        this.trackIntArray(this.tahonaData);
    }

    public TahonaContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, RegistryHandler.TAHONA_BLOCK.get());
    }

    private static TahonaTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");

        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());

        if(tileAtPos instanceof TahonaTileEntity){
            return (TahonaTileEntity) tileAtPos;
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
            if(index < this.playerInvStartIndex){
                if(!this.mergeItemStack(itemStack1, this.playerInvStartIndex, this.inventorySlots.size(), false)){
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemStack1, 0, this.playerInvStartIndex - 1, false)){
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


    @OnlyIn(Dist.CLIENT)
    public int getCookProgressionScaled(int scale) {
        int i = this.tahonaData.get(0);
        int j = this.tahonaData.get(1);
        if (j == 0) return 0;
        return i * scale / j ;
    }

    public TahonaTileEntity getTileEntity(){
        return tileEntity;
    }
}
