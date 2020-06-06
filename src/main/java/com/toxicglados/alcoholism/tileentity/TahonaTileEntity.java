package com.toxicglados.alcoholism.tileentity;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.container.TahonaContainer;
import com.toxicglados.alcoholism.core.recipes.TahonaRecipes;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TahonaTileEntity extends LockableTileEntity {

    // The number of cycles the tahona has made on this ingredient
    private int cyclesMade;
    // The number of cycles required for this item
    private int cyclesRequired;
    private String customName;

    private int tickCount = 0;

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

    public final IIntArray tahonaData = new IIntArray() {
        public int get(int index) {
            switch (index) {
                case 0:
                    return TahonaTileEntity.this.cyclesMade;
                case 1:
                    return TahonaTileEntity.this.cyclesRequired;
                default:
                    return 0;
            }
        }

        public void set(int index, int value) {
            switch (index) {
                case 0:
                    TahonaTileEntity.this.cyclesMade = value;
                    break;
                case 1:
                    TahonaTileEntity.this.cyclesRequired = value;
                    break;
            }
        }

        public int size() {
            return 2;
        }
    };

    private NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);
    protected int numPlayerUsing;
    //private IItemHandlerModifiable items = createHandler();
    //private LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> items);

    public TahonaTileEntity(final TileEntityType<?> tileEntityType){
        super(tileEntityType);
    }

    public TahonaTileEntity(){
        this(RegistryHandler.TAHONA_TILE_ENTITY.get());
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
        boolean cyclesShouldReset = stack.isEmpty() || !stack.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(stack, itemStack);
        this.contents.set(index, stack);
        if(stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }

        if(index == SLOT.INGREDIENT.getIndex() && cyclesShouldReset)
        {
            this.cyclesRequired = getCyclesRequired(stack);
            this.cyclesMade = 0;
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
        return new TranslationTextComponent("container.alcoholism.tahona_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new TahonaContainer(id, player, this);
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
        compound.putInt("CyclesMade", (short)this.cyclesMade);
        compound.putInt("CyclesRequired", (short)this.cyclesRequired);
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
        this.cyclesMade = compound.getInt("CyclesMade");
        this.cyclesRequired = compound.getInt("CyclesRequired");

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

    public static int getCyclesRequired(ItemStack itemStack){
        if (itemStack.isEmpty()) {
            return 0;
        }
        else {
            // Everything requires 10 cycles for now
            return 5;
        }
    }

    public void doCycle(){
        if(!this.world.isRemote){
            cyclesMade++;
            if(cyclesMade >= cyclesRequired){
                cyclesMade = 0;
                this.mashItem();
                this.markDirty();
            }
        }
    }

    private boolean canMash(){
        ItemStack resultItemStack = TahonaRecipes.instance().getCookingResult(this.contents.get(SLOT.INGREDIENT.getIndex()));
        ItemStack outputItemStack = this.contents.get(SLOT.OUTPUT.getIndex());
        int outputStackLimit = outputItemStack.getMaxStackSize();

        // If the result of the mashing is not empty (i.e. a real recipe)
        if (resultItemStack != ItemStack.EMPTY){
            // If the output item stack is empty than we're good
            if (outputItemStack.isEmpty()){
                return true;
            }
            // If the item in the output slot right now is different than the one that will be made
            // once the mashing is done than we cannot mash
            else if (resultItemStack.getItem() != outputItemStack.getItem()){
                Alcoholism.LOGGER.info("Different item in output from result.");
                return false;
            }
            // If the combined stack size would be more less than or equal
            // to the max stack size then we're good to mash
            else if(resultItemStack.getCount() + outputItemStack.getCount() <= outputStackLimit){
                return true;
            }
        }

        return false;
    }

    public void mashItem(){
        if(this.canMash()){
            ItemStack ingredientItemStack = this.contents.get(SLOT.INGREDIENT.getIndex());
            ItemStack resultItemStack = TahonaRecipes.instance().getCookingResult(ingredientItemStack);
            ItemStack outputItemStack = this.contents.get(SLOT.OUTPUT.getIndex());

            if (outputItemStack.isEmpty()){
                this.contents.set(SLOT.OUTPUT.getIndex(), resultItemStack.copy());
            }
            else if (resultItemStack.getItem() == outputItemStack.getItem()){
                outputItemStack.grow(resultItemStack.getCount());
            }
            else{
                Alcoholism.LOGGER.error("Tried to mash item while result is not equal to the item currently in the output slot.");
            }

            ingredientItemStack.shrink(1);
        }
    }

    public static int getPlayersUsing(IBlockReader reader, BlockPos pos) {
        BlockState blockState = reader.getBlockState(pos);
        if(blockState.hasTileEntity()){
            TileEntity tileEntity = reader.getTileEntity(pos);
            if(tileEntity instanceof TahonaTileEntity) {
                return ((TahonaTileEntity)tileEntity).numPlayerUsing;
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
