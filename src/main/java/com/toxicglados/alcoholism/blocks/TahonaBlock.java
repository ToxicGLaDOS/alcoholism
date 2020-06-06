package com.toxicglados.alcoholism.blocks;

import com.toxicglados.alcoholism.tileentity.DistilleryTileEntity;
import com.toxicglados.alcoholism.tileentity.TahonaTileEntity;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class TahonaBlock extends Block {

    public TahonaBlock() {
        super(Properties.create(Material.ROCK)
                .hardnessAndResistance(0.5f, 6.0f)
                .sound(SoundType.STONE)
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return RegistryHandler.TAHONA_TILE_ENTITY.get().create();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult result) {
        if(!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TahonaTileEntity) {
                if(player.isSneaking()){
                    ((TahonaTileEntity)tileEntity).doCycle();
                    return ActionResultType.SUCCESS;
                }
                else {
                    NetworkHooks.openGui((ServerPlayerEntity) player, (TahonaTileEntity) tileEntity, pos);
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.SUCCESS;

    }

}
