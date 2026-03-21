package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;
import java.util.LinkedHashSet;
import java.util.Set;
import mekanism.api.gear.ModuleData;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModuleHelper.class)
public abstract class ModuleHelperMixin {

    @Inject(method = "getSupported", at = @At("RETURN"), cancellable = true)
    private void strongermekasuit$inheritSupportedModules(Item item, CallbackInfoReturnable<Set<ModuleData<?>>> cir) {
        Item vanillaEquivalent = getVanillaEquivalent(item);
        if (vanillaEquivalent == null) {
            return;
        }
        Set<ModuleData<?>> inherited = ModuleHelper.get().getSupported(vanillaEquivalent);
        if (inherited.isEmpty()) {
            return;
        }
        Set<ModuleData<?>> merged = new LinkedHashSet<>(cir.getReturnValue());
        merged.addAll(inherited);
        cir.setReturnValue(Set.copyOf(merged));
    }

    @Inject(method = "getSupportedItems", at = @At("RETURN"), cancellable = true)
    private void strongermekasuit$inheritSupportedItems(Holder<ModuleData<?>> typeProvider, CallbackInfoReturnable<Set<Item>> cir) {
        Set<Item> merged = new LinkedHashSet<>(cir.getReturnValue());
        if (cir.getReturnValue().contains(MekanismItems.MEKASUIT_HELMET.asItem())) {
            merged.add(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get());
        }
        if (cir.getReturnValue().contains(MekanismItems.MEKASUIT_BODYARMOR.asItem())) {
            merged.add(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get());
        }
        if (cir.getReturnValue().contains(MekanismItems.MEKASUIT_PANTS.asItem())) {
            merged.add(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get());
        }
        if (cir.getReturnValue().contains(MekanismItems.MEKASUIT_BOOTS.asItem())) {
            merged.add(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get());
        }
        cir.setReturnValue(Set.copyOf(merged));
    }

    private static Item getVanillaEquivalent(Item item) {
        if (item == StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get()) {
            return MekanismItems.MEKASUIT_HELMET.asItem();
        }
        if (item == StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get()) {
            return MekanismItems.MEKASUIT_BODYARMOR.asItem();
        }
        if (item == StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get()) {
            return MekanismItems.MEKASUIT_PANTS.asItem();
        }
        if (item == StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get()) {
            return MekanismItems.MEKASUIT_BOOTS.asItem();
        }
        return null;
    }
}
