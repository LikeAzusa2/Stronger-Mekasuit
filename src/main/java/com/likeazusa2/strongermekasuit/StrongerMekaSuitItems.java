package com.likeazusa2.strongermekasuit;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitItem;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class StrongerMekaSuitItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StrongerMekaSuit.MODID);

    public static final DeferredItem<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_HELMET = ITEMS.register("advanced_mekasuit_helmet",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.HELMET, new net.minecraft.world.item.Item.Properties()));
    public static final DeferredItem<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_BODYARMOR = ITEMS.register("advanced_mekasuit_bodyarmor",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.CHESTPLATE, new net.minecraft.world.item.Item.Properties()));
    public static final DeferredItem<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_PANTS = ITEMS.register("advanced_mekasuit_pants",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.LEGGINGS, new net.minecraft.world.item.Item.Properties()));
    public static final DeferredItem<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_BOOTS = ITEMS.register("advanced_mekasuit_boots",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.BOOTS, new net.minecraft.world.item.Item.Properties()));

    private StrongerMekaSuitItems() {
    }
}
