package com.toxicglados.alcoholism.core.recipes;

import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.item.ItemStack;

public class BarrelRecipes extends AlcoholismRecipes {
    private static final BarrelRecipes SINGLETON_INSTANCE = new BarrelRecipes();

    private BarrelRecipes() {
        this.addRecipeFromItem(RegistryHandler.SILVER_TEQUILA.get(), new ItemStack(RegistryHandler.GOLD_TEQUILA.get()), 0.2f);
    }

    public static BarrelRecipes instance()
    {
        return SINGLETON_INSTANCE;
    }
}
