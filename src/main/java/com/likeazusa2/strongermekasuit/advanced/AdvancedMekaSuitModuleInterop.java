package com.likeazusa2.strongermekasuit.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuit;
import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;

import mekanism.api.MekanismIMC;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

public final class AdvancedMekaSuitModuleInterop {

    static final String HELMET_MODULE_METHOD = StrongerMekaSuit.MODID + ":add_advanced_mekasuit_helmet_modules";
    static final String BODYARMOR_MODULE_METHOD = StrongerMekaSuit.MODID + ":add_advanced_mekasuit_bodyarmor_modules";
    static final String PANTS_MODULE_METHOD = StrongerMekaSuit.MODID + ":add_advanced_mekasuit_pants_modules";
    static final String BOOTS_MODULE_METHOD = StrongerMekaSuit.MODID + ":add_advanced_mekasuit_boots_modules";

    private AdvancedMekaSuitModuleInterop() {
    }

    public static void enqueueIMC(InterModEnqueueEvent event) {
        // 仅注册进阶套是独立模块容器。
        // 实际支持模块列表改为继承原版 MekaSuit 同部位的结果，便于兼容附属模组。
        MekanismIMC.addModuleContainer(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get().builtInRegistryHolder(), HELMET_MODULE_METHOD);
        MekanismIMC.addModuleContainer(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get().builtInRegistryHolder(), BODYARMOR_MODULE_METHOD);
        MekanismIMC.addModuleContainer(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get().builtInRegistryHolder(), PANTS_MODULE_METHOD);
        MekanismIMC.addModuleContainer(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get().builtInRegistryHolder(), BOOTS_MODULE_METHOD);
    }
}
