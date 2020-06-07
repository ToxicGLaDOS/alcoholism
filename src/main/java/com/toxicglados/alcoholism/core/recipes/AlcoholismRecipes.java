package com.toxicglados.alcoholism.core.recipes;

import com.google.common.collect.Maps;
import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.util.RegistryHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Map;

public abstract class AlcoholismRecipes {

    protected final Map<ItemStack, ItemStack> recipeMap = Maps.<ItemStack, ItemStack>newHashMap();
    protected final Map<ItemStack, Float> experienceMap = Maps.<ItemStack, Float>newHashMap();

    public void addRecipeFromItem(Item input, ItemStack stack, float experience) {
        this.addRecipeFromItemStack(new ItemStack(input, 1), stack, experience);
    }

    public void addRecipeFromItemStack(ItemStack input, ItemStack stack, float experience) {
        if (getCookingResult(input) != ItemStack.EMPTY)
        {
            Alcoholism.LOGGER.warn("Ignored cooking recipe with conflicting input: {} = {}", input, stack);
            return;
        }
        this.recipeMap.put(input, stack);
        this.experienceMap.put(stack, Float.valueOf(experience));
    }

    public ItemStack getCookingResult(ItemStack stack) {
        for (Map.Entry<ItemStack, ItemStack> entry : this.recipeMap.entrySet())
        {
            if (stack.getItem() == entry.getKey().getItem())
            {
                return entry.getValue();
            }
        }

        return ItemStack.EMPTY;
    }
}
