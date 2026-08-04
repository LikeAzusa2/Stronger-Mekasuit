package com.likeazusa2.strongermekasuit.mixin;

import mekanism.api.NBTConstants;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiModuleScrollList.class, remap = false)
public abstract class GuiModuleScrollListMixin {

    @Shadow
    private ItemStack currentItem;

    @Inject(method = "recheckItem", at = @At("HEAD"), cancellable = true)
    private void strongermekasuit$ignoreVolatileNbtWhileTweakingModules(CallbackInfo ci) {
        ItemStack updated = currentItem;
        if (updated.isEmpty()) {
            return;
        }
        // The module tweaker only needs to rebuild when the actual module payload changes.
        // Energy, gas, fluid and other live values mutate every tick during flight/charging and otherwise
        // cause the entire module list/screen to be rebuilt continuously.
        ItemStack latest = ((GuiModuleScrollListAccessor) this).strongermekasuit$getItemSupplier().get();
        if (isSameModuleState(currentItem, latest)) {
            currentItem = latest;
            ci.cancel();
        }
    }

    private static boolean isSameModuleState(ItemStack current, ItemStack latest) {
        if (current == latest) {
            return true;
        }
        if (current.isEmpty() || latest.isEmpty()) {
            return current.isEmpty() && latest.isEmpty();
        }
        if (current.getItem() != latest.getItem() || current.getCount() != latest.getCount()) {
            return false;
        }
        CompoundTag currentModules = current.getTagElement(NBTConstants.MODULES);
        CompoundTag latestModules = latest.getTagElement(NBTConstants.MODULES);
        if (currentModules == null || latestModules == null) {
            return currentModules == null && latestModules == null;
        }
        return currentModules.equals(latestModules);
    }
}
