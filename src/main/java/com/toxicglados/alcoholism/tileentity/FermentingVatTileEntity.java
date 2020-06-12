package com.toxicglados.alcoholism.tileentity;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.container.FermentingVatContainer;
import com.toxicglados.alcoholism.core.recipes.FermentingVatRecipes;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FermentingVatTileEntity extends SidedTileEntity implements ITickableTileEntity {
    private static final int[] SLOTS_UP = new int[]{0};
    private static final int[] SLOTS_DOWN = new int[]{1};
    private static final int[] SLOTS_HORIZONTAL = new int[]{0};
    // Amount of ticks the current item has been fermenting
    private int fermentTime;
    // The number of ticks required to ferment the current item
    private int fermentTimeRequired;
    private String customName;

    private enum SLOT {
        INGREDIENT(0), OUTPUT(1);

        private final int index;
        private SLOT(int index){
            this.index = index;
        }

        public int getIndex(){
            return index;
        }
    }

    public final IIntArray fermentingVatData = new IIntArray() {
        public int get(int index) {
            switch (index) {
                case 0:
                    return FermentingVatTileEntity.this.fermentTime;
                case 1:
                    return FermentingVatTileEntity.this.fermentTimeRequired;
                default:
                    return 0;
            }
        }

        public void set(int index, int value) {
            switch (index) {
                case 0:
                    FermentingVatTileEntity.this.fermentTime = value;
                    break;
                case 1:
                    FermentingVatTileEntity.this.fermentTimeRequired = value;
                    break;
            }
        }

        public int size() {
            return 2;
        }
    };

    public FermentingVatTileEntity(final TileEntityType<?> tileEntityType){
        super(tileEntityType, 2);
    }

    public FermentingVatTileEntity(){
        this(RegistryHandler.FERMENTING_VAT_TILE_ENTITY.get());
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_DOWN;
        } else {
            return side == Direction.UP ? SLOTS_UP : SLOTS_HORIZONTAL;
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if(index == SLOT.INGREDIENT.getIndex()){
            return FermentingVatRecipes.instance().hasRecipeFor(stack);
        }
        else {
            return false;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack itemStack = this.contents.get(index);
        boolean cookTimeShouldReset = stack.isEmpty() || !stack.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(stack, itemStack);
        this.contents.set(index, stack);
        if(stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }

        if(index == FermentingVatTileEntity.SLOT.INGREDIENT.getIndex() && cookTimeShouldReset)
        {
            this.fermentTimeRequired = this.getFermentTime(stack);
            this.fermentTime = 0;
            this.markDirty();
        }
    }



    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.alcoholism.fermenting_vat_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new FermentingVatContainer(id, player, this);
    }

    // Called every time the chunk containing this tile entity is unloaded
    // Used to write data that will get loaded once the chunk is loaded again
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("FermentTime", (short)this.fermentTime);
        compound.putInt("FermentTimeRequired", (short)this.fermentTimeRequired);
        ItemStackHelper.saveAllItems(compound, this.contents);

        if(this.hasCustomName()){
            compound.putString("CustomName", this.customName);
        }
        return compound;
    }

    // Called every time the chunk containing this tile entity is loaded
    // Used to rehydrate this tile entity after getting unloaded
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.contents);
        this.fermentTime = compound.getInt("FermentTime");
        this.fermentTimeRequired = compound.getInt("FermentTimeRequired");

        // I think 8 is a magic number here that means "string"
        // as in; does this compound contain the key "CustomName" and its value is a string
        if (compound.contains("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if(id == 1){
            this.numPlayerUsing = type;
            return true;
        }
        else{
            return super.receiveClientEvent(id, type);
        }
    }



    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.UP)
                return handlers[0].cast();
            else if (facing == Direction.DOWN)
                return handlers[1].cast();
            else
                return handlers[2].cast();
        }
        return super.getCapability(capability, facing);
    }

    /**
     * invalidates a tile entity
     */
    @Override
    public void remove() {
        super.remove();
        for (int x = 0; x < handlers.length; x++)
            handlers[x].invalidate();
    }

    @Override
    public void tick() {
        if (!this.world.isRemote && this.canFerment())
        {
            this.fermentTime++;

            if(this.fermentTime == this.fermentTimeRequired){
                this.fermentItem();
                this.fermentTime = 0;
                this.markDirty();
            }
        }
    }

    private boolean canFerment(){
        ItemStack resultItemStack = FermentingVatRecipes.instance().getCookingResult(this.contents.get(FermentingVatTileEntity.SLOT.INGREDIENT.getIndex()));
        ItemStack outputItemStack = this.contents.get(FermentingVatTileEntity.SLOT.OUTPUT.getIndex());
        int outputStackLimit = outputItemStack.getMaxStackSize();

        // If the result of the fermentation is not empty (i.e. a real recipe)
        if (resultItemStack != ItemStack.EMPTY){
            // If the output item stack is empty than we're good
            if (outputItemStack.isEmpty()){
                return true;
            }
            // If the item in the output slot right now is different than the one that will be made
            // once the fermentation is done, than we cannot ferment
            else if (resultItemStack.getItem() != outputItemStack.getItem()){
                Alcoholism.LOGGER.info("Different item in output from result.");
                return false;
            }
            // If the combined stack size would be more less than or equal
            // to the max stack size then we're good to ferment
            else if(resultItemStack.getCount() + outputItemStack.getCount() <= outputStackLimit){
                return true;
            }
        }

        return false;
    }

    public void fermentItem(){
        if(this.canFerment()){
            ItemStack ingredientItemStack = this.contents.get(FermentingVatTileEntity.SLOT.INGREDIENT.getIndex());
            ItemStack resultItemStack = FermentingVatRecipes.instance().getCookingResult(ingredientItemStack);
            ItemStack outputItemStack = this.contents.get(FermentingVatTileEntity.SLOT.OUTPUT.getIndex());

            if (outputItemStack.isEmpty()){
                this.contents.set(FermentingVatTileEntity.SLOT.OUTPUT.getIndex(), resultItemStack.copy());
            }
            else if (resultItemStack.getItem() == outputItemStack.getItem()){
                outputItemStack.grow(resultItemStack.getCount());
            }
            else{
                Alcoholism.LOGGER.error("Tried to ferment item while result is not equal to the item currently in the output slot.");
            }

            ingredientItemStack.shrink(1);
        }
    }

    public int getFermentTime(ItemStack itemStack){
        if(itemStack.isEmpty()){
            return 0;
        }
        else {
            return 100;
        }
    }

    public static int getPlayersUsing(IBlockReader reader, BlockPos pos) {
        BlockState blockState = reader.getBlockState(pos);
        if(blockState.hasTileEntity()){
            TileEntity tileEntity = reader.getTileEntity(pos);
            if(tileEntity instanceof FermentingVatTileEntity) {
                return ((FermentingVatTileEntity)tileEntity).numPlayerUsing;
            }
        }
        return 0;
    }

    private void playSound(SoundEvent sound){
        double dx = (double)this.pos.getX() + 0.5D;
        double dy = (double)this.pos.getY() + 0.5D;
        double dz = (double)this.pos.getZ() + 0.5D;

        this.world.playSound((PlayerEntity)null, dx, dy, dz, sound, SoundCategory.BLOCKS, 0.5f, this.world.rand.nextFloat() * 0.1f + 0.9f);
    }

}
