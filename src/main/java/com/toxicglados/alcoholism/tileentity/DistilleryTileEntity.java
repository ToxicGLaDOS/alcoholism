package com.toxicglados.alcoholism.tileentity;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.container.DistilleryContainer;
import com.toxicglados.alcoholism.core.recipes.DistilleryRecipes;
import com.toxicglados.alcoholism.core.slots.DistilleryFuelSlot;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistilleryTileEntity extends SidedTileEntity implements ITickableTileEntity {
    private static final int[] SLOTS_UP = new int[]{0};
    private static final int[] SLOTS_DOWN = new int[]{2};
    private static final int[] SLOTS_HORIZONTAL = new int[]{1};

    // Amount of ticks left of fuel for this item
    private int fuelBurnTimeRemaining;
    // The number of ticks this item gives in burn time
    private int currentItemBurnTime;
    // The amount of ticks the current ingredient has been cooked for
    private int cookTime;
    // The amount of ticks required to cook the current item
    private int totalCookTime;
    private String customName;

    private enum SLOT {
        INGREDIENT(0), FUEL(1), OUTPUT(2);

        private final int index;
        private SLOT(int index){
            this.index = index;
        }

        public int getIndex(){
            return index;
        }
    }

    public final IIntArray distilleryData = new IIntArray() {
        public int get(int index) {
            switch (index) {
                case 0:
                    return DistilleryTileEntity.this.fuelBurnTimeRemaining;
                case 1:
                    return DistilleryTileEntity.this.currentItemBurnTime;
                case 2:
                    return DistilleryTileEntity.this.cookTime;
                case 3:
                    return DistilleryTileEntity.this.totalCookTime;
                default:
                    return 0;
            }
        }

        public void set(int index, int value) {
            switch (index) {
                case 0:
                    DistilleryTileEntity.this.fuelBurnTimeRemaining = value;
                    break;
                case 1:
                    DistilleryTileEntity.this.currentItemBurnTime = value;
                    break;
                case 2:
                    DistilleryTileEntity.this.cookTime = value;
                    break;
                case 3:
                    DistilleryTileEntity.this.totalCookTime = value;
            }
        }

        public int size() {
            return 4;
        }
    };

    public DistilleryTileEntity(final TileEntityType<?> tileEntityType){
        super(tileEntityType, 3);
    }

    public DistilleryTileEntity(){
        this(RegistryHandler.DISTILLERY_TILE_ENTITY.get());
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
        if (index == SLOT.INGREDIENT.getIndex()){
            return DistilleryRecipes.instance().hasRecipeFor(stack);
        }
        else if(index == SLOT.FUEL.getIndex()){
            return DistilleryFuelSlot.isFuel(stack);
        }
        else{
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

        if(index == SLOT.INGREDIENT.getIndex() && cookTimeShouldReset)
        {
            this.totalCookTime = this.getCookTime(stack);
            this.cookTime = 0;
            this.markDirty();
        }
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.alcoholism.distillery_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new DistilleryContainer(id, player, this);
    }

    // Called every time the chunk containing this tile entity is unloaded
    // Used to write data that will get loaded once the chunk is loaded again
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("FuelBurnTime", (short)this.fuelBurnTimeRemaining);
        compound.putInt("CookTime", (short)this.cookTime);
        compound.putInt("CookTimeTotal", (short)this.totalCookTime);
        compound.putInt("CurrentItemBurnTime", (short)this.currentItemBurnTime);
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
        this.fuelBurnTimeRemaining = compound.getInt("FuelBurnTime");
        this.cookTime = compound.getInt("CookTime");
        this.totalCookTime = compound.getInt("CookTimeTotal");
        this.currentItemBurnTime = compound.getInt("CurrentItemBurnTime");

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
        boolean wasBurning = this.isBurning();
        boolean isDirty = false;
        if (this.isBurning()){
            this.fuelBurnTimeRemaining--;
        }

        if (!this.world.isRemote)
        {
            ItemStack ingredientStack = this.contents.get(SLOT.INGREDIENT.getIndex());
            ItemStack fuelStack = this.contents.get(SLOT.FUEL.getIndex());
            // If we're not burning anymore than a fuel has been consumed
            // and we need to check if there is any left to keep going
            if (!this.isBurning() && this.canDistill()){

                // Set the burn time remaining to the burn time of the fuel in the slot right now
                this.fuelBurnTimeRemaining = getBurnTime(fuelStack);
                this.currentItemBurnTime = this.fuelBurnTimeRemaining;

                // If we are now burning again than we need to consume a fuel
                if (this.isBurning()){

                    isDirty = true;

                    Item fuelItem = fuelStack.getItem();
                    fuelStack.shrink(1);

                }
            }

            if (this.isBurning() && this.canDistill()) {
                this.cookTime++;

                if (this.cookTime == this.totalCookTime) {
                    this.cookTime = 0;
                    this.totalCookTime = this.getCookTime(this.contents.get(0));
                    this.distillItem();
                    isDirty = true;
                }
            }
            else {
                this.cookTime = 0;
            }
            if (wasBurning != this.isBurning()) {
                isDirty = true;
                //DistilleryBlock.setState(this.isBurning(), this.world, this.pos);
            }
        }

        if (isDirty){
            this.markDirty();
        }
    }

    public static int getBurnTime(ItemStack itemStack){
        if (itemStack.isEmpty()) {
            return 0;
        }
        else {
            int burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(itemStack);
            if(burnTime >= 0){
                return burnTime;
            }
            else{
                Alcoholism.LOGGER.error("Negative burn time found for fuel. Burn time: {}", burnTime);
                return 0;
            }
        }
    }

    private boolean canDistill(){
        ItemStack resultItemStack = DistilleryRecipes.instance().getCookingResult(this.contents.get(SLOT.INGREDIENT.getIndex()));
        ItemStack outputItemStack = this.contents.get(SLOT.OUTPUT.getIndex());
        int outputStackLimit = outputItemStack.getMaxStackSize();

        // If the result of the distillation is not empty (i.e. a real recipe)
        if (resultItemStack != ItemStack.EMPTY){
            // If the output item stack is empty than we're good
            if (outputItemStack.isEmpty()){
                return true;
            }
            // If the item in the output slot right now is different than the one that will be made
            // once the distilling is done than we cannot distill
            else if (resultItemStack.getItem() != outputItemStack.getItem()){
                Alcoholism.LOGGER.info("Different item in output from result.");
                return false;
            }
            // If the combined stack size would be more less than or equal
            // to the max stack size then we're good to distill
            else if(resultItemStack.getCount() + outputItemStack.getCount() <= outputStackLimit){
                return true;
            }
        }

        return false;
    }

    public void distillItem(){
        if(this.canDistill()){
            ItemStack ingredientItemStack = this.contents.get(SLOT.INGREDIENT.getIndex());
            ItemStack resultItemStack = DistilleryRecipes.instance().getCookingResult(ingredientItemStack);
            ItemStack outputItemStack = this.contents.get(SLOT.OUTPUT.getIndex());

            if (outputItemStack.isEmpty()){
                this.contents.set(SLOT.OUTPUT.getIndex(), resultItemStack.copy());
            }
            else if (resultItemStack.getItem() == outputItemStack.getItem()){
                outputItemStack.grow(resultItemStack.getCount());
            }
            else{
                Alcoholism.LOGGER.error("Tried to distill item while result is not equal to the item currently in the output slot.");
            }

            ingredientItemStack.shrink(1);
        }
    }

    public boolean isBurning(){
        return this.fuelBurnTimeRemaining > 0;
    }

    public int getCookTime(ItemStack itemStack){
        return 100;
    }

    public static int getPlayersUsing(IBlockReader reader, BlockPos pos) {
        BlockState blockState = reader.getBlockState(pos);
        if(blockState.hasTileEntity()){
            TileEntity tileEntity = reader.getTileEntity(pos);
            if(tileEntity instanceof DistilleryTileEntity) {
                return ((DistilleryTileEntity)tileEntity).numPlayerUsing;
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
