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

public class TahonaRecipes {

    private static final TahonaRecipes COOKING_BASE = new TahonaRecipes();
    private final Map<ItemStack, ItemStack> cookingList = Maps.<ItemStack, ItemStack>newHashMap();
    private final Map<ItemStack, Float> experienceList = Maps.<ItemStack, Float>newHashMap();

    private TahonaRecipes() {
        this.addCooking(RegistryHandler.AGAVE.get(), new ItemStack(RegistryHandler.BAKED_AGAVE.get()), 0.2f);
    }

    public static TahonaRecipes instance()
    {
        return COOKING_BASE;
    }

    public void addCooking(Item input, ItemStack stack, float experience) {
        this.addCookingRecipe(new ItemStack(input, 1), stack, experience);
    }

    public void addCookingRecipe(ItemStack input, ItemStack stack, float experience) {
        if (getCookingResult(input) != ItemStack.EMPTY)
        {
            Alcoholism.LOGGER.warn("Ignored cooking recipe with conflicting input: {} = {}", input, stack);
            return;
        }
        this.cookingList.put(input, stack);
        this.experienceList.put(stack, Float.valueOf(experience));
    }

    public ItemStack getCookingResult(ItemStack stack) {
        for (Entry<ItemStack, ItemStack> entry : this.cookingList.entrySet())
        {
            if (stack.getItem() == entry.getKey().getItem())
            {
                return entry.getValue();
            }
        }

        return ItemStack.EMPTY;
    }
}
