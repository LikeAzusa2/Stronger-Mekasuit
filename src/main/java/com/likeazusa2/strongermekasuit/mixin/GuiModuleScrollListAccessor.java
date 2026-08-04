package com.likeazusa2.strongermekasuit.mixin;

import java.util.function.Supplier;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiModuleScrollList.class, remap = false)
public interface GuiModuleScrollListAccessor {

    @Accessor("itemSupplier")
    Supplier<ItemStack> strongermekasuit$getItemSupplier();
}
