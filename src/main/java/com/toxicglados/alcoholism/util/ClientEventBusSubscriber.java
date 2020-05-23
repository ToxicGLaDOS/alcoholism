package com.toxicglados.alcoholism.util;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.client.gui.DistilleryScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Alcoholism.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event){
        ScreenManager.registerFactory(RegistryHandler.DISTILLERY_CONTAINER.get(), DistilleryScreen::new);
    }

}
