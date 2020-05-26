package com.toxicglados.alcoholism.util;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.blocks.BlockItemBase;
import com.toxicglados.alcoholism.blocks.DistilleryBlock;
import com.toxicglados.alcoholism.container.DistilleryContainer;
import com.toxicglados.alcoholism.items.ItemBase;
import com.toxicglados.alcoholism.tileentity.DistilleryTileEntity;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryHandler {

    // Only registers items
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Alcoholism.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Alcoholism.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Alcoholism.MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Alcoholism.MOD_ID);




    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register((FMLJavaModLoadingContext.get().getModEventBus()));
    }

    // Items
    public static final RegistryObject<Item> VODKA = ITEMS.register("vodka", ItemBase::new);

    // Blocks
    public static final RegistryObject<Block> DISTILLERY_BLOCK = BLOCKS.register("distillery_block", DistilleryBlock::new);

    // Block Items
    public static final RegistryObject<Item> DISTILLERY_BLOCK_ITEM = ITEMS.register("distillery_block", () -> new BlockItemBase(DISTILLERY_BLOCK.get()));

    // Tile Entities
    public static final RegistryObject<TileEntityType<DistilleryTileEntity>> DISTILLERY_TILE_ENTITY =  TILE_ENTITIES.register("distillery_tile_entity", () ->
                                                                                                                        TileEntityType.Builder.create(DistilleryTileEntity::new, DISTILLERY_BLOCK.get())
                                                                                                                        .build(null));

    // Containers
    public static final RegistryObject<ContainerType<DistilleryContainer>> DISTILLERY_CONTAINER = CONTAINERS.register("distillery_container", () -> IForgeContainerType.create(DistilleryContainer::new));
}