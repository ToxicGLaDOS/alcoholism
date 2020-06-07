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

public class DistilleryRecipes extends AlcoholismRecipes{

    protected static final DistilleryRecipes SINGLETON_INSTANCE = new DistilleryRecipes();

    private DistilleryRecipes() {
        this.addRecipeFromItem(Items.POTATO, new ItemStack(RegistryHandler.VODKA.get()), 0.2f);
        this.addRecipeFromItem(RegistryHandler.MOSTO.get(), new ItemStack(RegistryHandler.ORDINARIO.get()), 0.2f);
        this.addRecipeFromItem(RegistryHandler.ORDINARIO.get(), new ItemStack(RegistryHandler.SILVER_TEQUILA.get()), 0.2f);

    }

    public static DistilleryRecipes instance()
    {
        return SINGLETON_INSTANCE;
    }

}
