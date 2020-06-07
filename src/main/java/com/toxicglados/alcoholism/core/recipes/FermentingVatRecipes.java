package com.toxicglados.alcoholism.core.recipes;

import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.item.ItemStack;

public class FermentingVatRecipes extends AlcoholismRecipes {

    private static final FermentingVatRecipes SINGLETON_INSTANCE = new FermentingVatRecipes();

    private FermentingVatRecipes() {
        this.addRecipeFromItem(RegistryHandler.AGAVE_JUICE.get(), new ItemStack(RegistryHandler.MOSTO.get()), 0.2f);
    }

    public static FermentingVatRecipes instance()
    {
        return SINGLETON_INSTANCE;
    }

}
