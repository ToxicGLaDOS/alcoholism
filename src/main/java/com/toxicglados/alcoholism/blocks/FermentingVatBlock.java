package com.toxicglados.alcoholism.blocks;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.tileentity.FermentingVatTileEntity;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class FermentingVatBlock extends Block {
    public FermentingVatBlock() {
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
        return RegistryHandler.FERMENTING_VAT_TILE_ENTITY.get().create();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof FermentingVatTileEntity) {
                InventoryHelper.dropInventoryItems(worldIn, pos, (FermentingVatTileEntity)tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult result) {
        if(!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            Alcoholism.LOGGER.info("Instance?");
            if(tileEntity instanceof FermentingVatTileEntity) {
                Alcoholism.LOGGER.info("Activate");
                NetworkHooks.openGui((ServerPlayerEntity) player, (FermentingVatTileEntity) tileEntity, pos);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;

    }
}
