package com.likeazusa2.strongermekasuit.client;

import com.likeazusa2.strongermekasuit.StrongerMekaSuit;
import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;
import mekanism.api.gear.IModuleHelper;
import mekanism.client.render.armor.MekaSuitArmor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = StrongerMekaSuit.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class StrongerMekaSuitClientRegistration {

    private StrongerMekaSuitClientRegistration() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(MekaSuitArmor.HELMET, StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get());
        event.registerItem(MekaSuitArmor.BODYARMOR, StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get());
        event.registerItem(MekaSuitArmor.PANTS, StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get());
        event.registerItem(MekaSuitArmor.BOOTS, StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> IModuleHelper.INSTANCE.addMekaSuitModuleModels(
              ResourceLocation.fromNamespaceAndPath(StrongerMekaSuit.MODID, "models/entity/advanced_mekasuit_modules.obj")
        ));
    }
}
