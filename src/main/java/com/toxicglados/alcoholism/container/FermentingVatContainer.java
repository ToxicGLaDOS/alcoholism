package com.toxicglados.alcoholism.container;

import com.toxicglados.alcoholism.core.slots.FermentingVatSlot;
import com.toxicglados.alcoholism.core.slots.OutputSlot;
import com.toxicglados.alcoholism.tileentity.FermentingVatTileEntity;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

public class FermentingVatContainer extends SingleOutputContainer {

    public final FermentingVatTileEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;
    private final IIntArray fermentingVatData;

    public FermentingVatContainer(final int windowId, final PlayerInventory playerInventory, final FermentingVatTileEntity tileEntity) {
        super(RegistryHandler.FERMENTING_VAT_CONTAINER.get(), windowId, 2);
        this.tileEntity = tileEntity;
        this.fermentingVatData = tileEntity.fermentingVatData;

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
        this.addSlot(new FermentingVatSlot(tileEntity, 0, ingredientInputX, ingredientInputY));

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
        // IT KEEPS THE fermentingVatData IN SYNC WITH CLIENT
        this.trackIntArray(this.fermentingVatData);
    }

    public FermentingVatContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, RegistryHandler.FERMENTING_VAT_BLOCK.get());
    }

    private static FermentingVatTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");

        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());

        if(tileAtPos instanceof FermentingVatTileEntity){
            return (FermentingVatTileEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    @OnlyIn(Dist.CLIENT)
    public int getCookProgressionScaled(int scale) {
        int i = this.fermentingVatData.get(0);
        int j = this.fermentingVatData.get(1);
        if (j == 0) return 0;
        return i * scale / j ;
    }

    public FermentingVatTileEntity getTileEntity(){
        return tileEntity;
    }

}
