package com.likeazusa2.strongermekasuit;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class StrongerMekaSuitItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StrongerMekaSuit.MODID);

    public static final RegistryObject<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_HELMET = ITEMS.register("advanced_mekasuit_helmet",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_BODYARMOR = ITEMS.register("advanced_mekasuit_bodyarmor",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_PANTS = ITEMS.register("advanced_mekasuit_pants",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<AdvancedMekaSuitItem> ADVANCED_MEKASUIT_BOOTS = ITEMS.register("advanced_mekasuit_boots",
          () -> new AdvancedMekaSuitItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    private StrongerMekaSuitItems() {
    }
}

