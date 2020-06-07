package com.toxicglados.alcoholism.blocks;

import com.toxicglados.alcoholism.tileentity.DistilleryTileEntity;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

public class DistilleryBlock extends Block {

    public DistilleryBlock() {
        super(Block.Properties.create(Material.IRON)
                .hardnessAndResistance(0.5f, 6.0f)
                .sound(SoundType.ANVIL)
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return RegistryHandler.DISTILLERY_TILE_ENTITY.get().create();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof DistilleryTileEntity) {
                InventoryHelper.dropInventoryItems(worldIn, pos, (DistilleryTileEntity)tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult result) {
        if(!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof DistilleryTileEntity) {
                // Furnace uses this method: player.openContainer((INamedContainerProvider)tileEntity);
                // But it doesn't seem to work (it gives a null PacketBuffer to the DistilleryContainer constructor)
                // This opengui method seems to do the trick though
                NetworkHooks.openGui((ServerPlayerEntity) player, (DistilleryTileEntity) tileEntity, pos);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;

    }

}
