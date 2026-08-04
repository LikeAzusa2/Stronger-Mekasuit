package com.likeazusa2.strongermekasuit.client;

import com.likeazusa2.strongermekasuit.StrongerMekaSuit;
import mekanism.api.gear.IModuleHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = StrongerMekaSuit.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class StrongerMekaSuitClientRegistration {

    private StrongerMekaSuitClientRegistration() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> IModuleHelper.INSTANCE.addMekaSuitModuleModels(
              ResourceLocation.fromNamespaceAndPath(StrongerMekaSuit.MODID, "models/entity/advanced_mekasuit_modules.obj")
        ));
    }
}

