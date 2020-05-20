package com.toxicglados.alcoholism.tileentity;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.container.DistilleryContainer;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
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

public class DistilleryTileEntity extends LockableLootTileEntity implements ITickableTileEntity {

    private int progress = 0;
    private int totalTicksToCompletion = 100;
    private NonNullList<ItemStack> contents = NonNullList.withSize(36, ItemStack.EMPTY);
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
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.alcohol.distillery_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new DistilleryContainer(id, player, this);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.contents;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.contents = itemsIn;
    }

    @Override
    public int getSizeInventory() {
        return 36;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if(!this.checkLootAndWrite(compound)){
            ItemStackHelper.saveAllItems(compound, this.contents);
        }
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if(!this.checkLootAndRead(compound)){
            ItemStackHelper.loadAllItems(compound, this.contents);
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
        /* BlockState blockState = world.getBlockState(pos);
        Block.spawnDrops(blockState, world, this.pos.add(0, 1.5, 0));
        Minecraft.getInstance().player.sendChatMessage("###########TESTING###########");
        Alcoholism.LOGGER.info("###########TESTING###########");
        */
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

    public static void swapContents(DistilleryTileEntity tileEntity, DistilleryTileEntity otherTileEntity) {
        NonNullList<ItemStack> list = tileEntity.getItems();
        tileEntity.setItems(otherTileEntity.getItems());
        otherTileEntity.setItems(list);
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
