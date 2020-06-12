package com.toxicglados.alcoholism.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;

// This class serves as a base class for classes that should inherit from
// LockableTileEntity with some reasonable default behaviours
public abstract class AlcoholismLockableTileEntity extends LockableTileEntity {
    protected NonNullList<ItemStack> contents;
    protected int numPlayerUsing;

    protected AlcoholismLockableTileEntity(TileEntityType<?> typeIn, final int containerSize) {
        super(typeIn);
        contents = NonNullList.withSize(containerSize, ItemStack.EMPTY);
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemStack : contents){
            if(!itemStack.isEmpty()){
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.contents.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.contents, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.contents, index);
    }

    @Override
    public void clear() {
        this.contents.clear();
    }

    @Override
    public int getSizeInventory() {
        return this.contents.size();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        // Not sure how this could happen?
        if(this.world.getTileEntity(this.pos) != this)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if(!player.isSpectator()) {
            if(this.numPlayerUsing < 0) {
                this.numPlayerUsing = 0;
            }
            this.numPlayerUsing++;
            this.onOpenOrClose();
        }
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        if(!player.isSpectator()) {
            this.numPlayerUsing--;
            this.onOpenOrClose();
        }
    }

    protected void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        if(block instanceof DispenserBlock){
            this.world.addBlockEvent(this.pos, block, 1, this.numPlayerUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, block);
        }
    }
}
