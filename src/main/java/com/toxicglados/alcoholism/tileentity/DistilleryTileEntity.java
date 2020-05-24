package com.toxicglados.alcoholism.tileentity;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.blocks.DistilleryBlock;
import com.toxicglados.alcoholism.container.DistilleryContainer;
import com.toxicglados.alcoholism.core.recipes.DistilleryRecipes;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

public class DistilleryTileEntity extends LockableTileEntity implements ITickableTileEntity {

    private int fuelBurnTimeRemaining;
    private int currentItemBurnTime;
    private int cookTime;
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

    private NonNullList<ItemStack> contents = NonNullList.withSize(3, ItemStack.EMPTY);
    protected int numPlayerUsing;
    private IItemHandlerModifiable items = createHandler();
    private LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> items);

    public DistilleryTileEntity(final TileEntityType<?> tileEntityType){
        super(tileEntityType);
    }

    public DistilleryTileEntity(){
        this(RegistryHandler.DISTILLERY_TILE_ENTITY.get());
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
    public void clear() {
        this.contents.clear();
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.alcohol.distillery_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new DistilleryContainer(id, player, this);
    }

    @Override
    public int getSizeInventory() {
        return this.contents.size();
    }

    // Called every time the chunk containing this tile entity is unloaded
    // Used to write data that will get loaded once the chunk is loaded again
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("FuelBurnTime", (short)this.fuelBurnTimeRemaining);
        compound.putInt("CookTime", (short)this.cookTime);
        compound.putInt("CookTimeTotal", (short)this.totalCookTime);
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
        this.currentItemBurnTime = getBurnTime(this.contents.get(SLOT.FUEL.getIndex()));

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

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        if(this.itemHandler != null){
            this.itemHandler.invalidate();
            this.itemHandler = null;
        }
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nonnull Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void remove() {
        super.remove();
        if(itemHandler != null){
            itemHandler.invalidate();
        }
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
            if (!this.isBurning() && this.canSmelt()){
                Alcoholism.LOGGER.info("Attempt to consume fuel");

                // Set the burn time remaining to the burn time of the fuel in the slot right now
                this.fuelBurnTimeRemaining = getBurnTime(fuelStack);
                this.currentItemBurnTime = this.fuelBurnTimeRemaining;
                Alcoholism.LOGGER.info("Fuel stack burn time: {}", getBurnTime(fuelStack));

                // If we are now burning again than we need to consume a fuel
                if (this.isBurning()){

                    isDirty = true;

                    Item fuelItem = fuelStack.getItem();
                    fuelStack.shrink(1);
                    Alcoholism.LOGGER.info("Consumed fuel.");

                }
            }

            if (this.isBurning() && this.canSmelt()) {
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
            Alcoholism.LOGGER.info("Burn time: {}", burnTime);
            if(burnTime >= 0){
                return burnTime;
            }
            else{
                Alcoholism.LOGGER.error("Negative burn time found for fuel. Burn time: {}", burnTime);
                return 0;
            }
        }
    }

    private boolean canSmelt(){
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
        if(this.canSmelt()){
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
        return 10;
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

    private IItemHandlerModifiable createHandler() {
        return new InvWrapper(this);
    }

    private void playSound(SoundEvent sound){
        double dx = (double)this.pos.getX() + 0.5D;
        double dy = (double)this.pos.getY() + 0.5D;
        double dz = (double)this.pos.getZ() + 0.5D;

        this.world.playSound((PlayerEntity)null, dx, dy, dz, sound, SoundCategory.BLOCKS, 0.5f, this.world.rand.nextFloat() * 0.1f + 0.9f);
    }



}
