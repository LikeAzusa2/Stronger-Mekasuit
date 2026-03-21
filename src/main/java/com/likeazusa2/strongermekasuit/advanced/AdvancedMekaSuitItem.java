package com.likeazusa2.strongermekasuit.advanced;

import mekanism.common.item.gear.ItemMekaSuitArmor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedMekaSuitItem extends ItemMekaSuitArmor {

    public AdvancedMekaSuitItem(ArmorItem.Type armorType, Item.Properties properties) {
        super(armorType, properties);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        // 原版 MekaSuit 的附魔值为 0，这里单独放开，让高级套装走正常护甲附魔规则。
        return stack.getMaxStackSize() == 1;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        // 禁用物品和模型的附魔流光。
        return false;
    }

    public float getBypassArmorAbsorption() {
        // 不再提供可配置倍率，直接沿用官方各部位的基础吸收值。
        return getOfficialAbsorption();
    }

    public float getOfficialAbsorption() {
        return switch (getType()) {
            case HELMET, BOOTS -> 0.15F;
            case CHESTPLATE -> 0.4F;
            case LEGGINGS -> 0.3F;
            default -> throw new IllegalStateException("Unexpected armor type: " + getType());
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }
}
