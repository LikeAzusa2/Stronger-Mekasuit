package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// remap is false: ModuleHelper.getSupported is a Mekanism-owned method, but the item overload's
// descriptor references ItemStack. Matching by name (no literal descriptor) lets Mixin use the
// reobfuscated handler signature (SRG ItemStack) so the injection also applies against the production jar,
// where Mekanism's own methods keep their names but Minecraft types are SRG-renamed.
@Mixin(value = ModuleHelper.class, remap = false)
public abstract class ModuleHelperMixin {

    private static final Map<Item, Set<ModuleData<?>>> SUPPORTED_MODULE_CACHE = new ConcurrentHashMap<>();
    private static final Map<ModuleData<?>, Set<Item>> SUPPORTED_ITEM_CACHE = new ConcurrentHashMap<>();

    @Inject(
          method = "getSupported",
          at = @At("RETURN"),
          cancellable = true,
          require = 0
    )
    private void strongermekasuit$inheritSupportedModules(ItemStack stack, CallbackInfoReturnable<Set<ModuleData<?>>> cir) {
        Item vanillaEquivalent = StrongerMekaSuitItems.getVanillaEquivalent(stack.getItem());
        if (vanillaEquivalent == null) {
            return;
        }
        Set<ModuleData<?>> cached = SUPPORTED_MODULE_CACHE.get(stack.getItem());
        if (cached == null) {
            // Cache the merged support table once so the module tweaker GUI does not keep rebuilding sets while it refreshes.
            Set<ModuleData<?>> inherited = ModuleHelper.get().getSupported(new ItemStack(vanillaEquivalent));
            if (inherited.isEmpty()) {
                return;
            }
            Set<ModuleData<?>> merged = new LinkedHashSet<>(cir.getReturnValue());
            merged.addAll(inherited);
            cached = Set.copyOf(merged);
            SUPPORTED_MODULE_CACHE.put(stack.getItem(), cached);
        }
        cir.setReturnValue(cached);
    }

    @Inject(
          method = "getSupported(Lmekanism/api/providers/IModuleDataProvider;)Ljava/util/Set;",
          at = @At("RETURN"),
          cancellable = true,
          require = 0
    )
    private void strongermekasuit$inheritSupportedItems(IModuleDataProvider<?> typeProvider, CallbackInfoReturnable<Set<Item>> cir) {
        ModuleData<?> moduleData = typeProvider.getModuleData();
        Set<Item> cached = SUPPORTED_ITEM_CACHE.get(moduleData);
        if (cached == null) {
            Set<Item> merged = new LinkedHashSet<>(cir.getReturnValue());
            // Mirror all vanilla MekaSuit-supported items to their advanced counterparts.
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
            cached = Set.copyOf(merged);
            SUPPORTED_ITEM_CACHE.put(moduleData, cached);
        }
        cir.setReturnValue(cached);
    }

}
