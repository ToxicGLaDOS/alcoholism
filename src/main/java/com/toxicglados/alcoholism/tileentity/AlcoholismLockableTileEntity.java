package com.toxicglados.alcoholism.tileentity;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;

// This class serves as a base class for classes that should inherit from
// LockableTileEntity with some reasonable default behaviours
public abstract class AlcoholismLockableTileEntity extends LockableTileEntity {
    protected NonNullList<ItemStack> contents;

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
}
