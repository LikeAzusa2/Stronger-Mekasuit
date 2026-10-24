package com.likeazusa2.strongermekasuit;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import mekanism.common.registries.MekanismItems;
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

    public static boolean isAdvancedPiece(ItemStack stack) {
        return stack != null && !stack.isEmpty()
              && (stack.is(ADVANCED_MEKASUIT_HELMET.get())
              || stack.is(ADVANCED_MEKASUIT_BODYARMOR.get())
              || stack.is(ADVANCED_MEKASUIT_PANTS.get())
              || stack.is(ADVANCED_MEKASUIT_BOOTS.get()));
    }

    public static boolean isAdvancedBodyArmor(ItemStack stack) {
        return stack != null && stack.is(ADVANCED_MEKASUIT_BODYARMOR.get());
    }

    public static Item getVanillaEquivalent(Item item) {
        if (item == ADVANCED_MEKASUIT_HELMET.get()) {
            return MekanismItems.MEKASUIT_HELMET.asItem();
        }
        if (item == ADVANCED_MEKASUIT_BODYARMOR.get()) {
            return MekanismItems.MEKASUIT_BODYARMOR.asItem();
        }
        if (item == ADVANCED_MEKASUIT_PANTS.get()) {
            return MekanismItems.MEKASUIT_PANTS.asItem();
        }
        if (item == ADVANCED_MEKASUIT_BOOTS.get()) {
            return MekanismItems.MEKASUIT_BOOTS.asItem();
        }
        return null;
    }

    private StrongerMekaSuitItems() {
    }
}

