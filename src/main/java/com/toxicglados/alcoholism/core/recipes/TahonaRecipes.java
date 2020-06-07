package com.toxicglados.alcoholism.core.recipes;

import com.google.common.collect.Maps;
import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Map;
import java.util.Map.Entry;

public class TahonaRecipes extends AlcoholismRecipes{

    private static final TahonaRecipes SINGLETON_INSTANCE = new TahonaRecipes();

    private TahonaRecipes() {
        this.addRecipeFromItem(RegistryHandler.BAKED_AGAVE.get(), new ItemStack(RegistryHandler.AGAVE_JUICE.get()), 0.2f);
    }

    public static TahonaRecipes instance()
    {
        return SINGLETON_INSTANCE;
    }

}
