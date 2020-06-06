package com.toxicglados.alcoholism.util;

import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.client.gui.DistilleryScreen;
import com.toxicglados.alcoholism.client.gui.TahonaScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
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
        ScreenManager.registerFactory(RegistryHandler.TAHONA_CONTAINER.get(), TahonaScreen::new);

        // Setting the render layer on these blocks to getCutout means that it won't
        // render alpha only pixels in the texture
        RenderTypeLookup.setRenderLayer(RegistryHandler.RICE_CROP.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RegistryHandler.AGAVE_CROP.get(), RenderType.getCutout());
    }

}
