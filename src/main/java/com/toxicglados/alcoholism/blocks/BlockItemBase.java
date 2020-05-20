package com.toxicglados.alcoholism.blocks;

import com.toxicglados.alcoholism.Alcoholism;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class BlockItemBase extends BlockItem {


    public BlockItemBase(Block block) {
        super(block, new Item.Properties().group(Alcoholism.TAB));
    }
}
