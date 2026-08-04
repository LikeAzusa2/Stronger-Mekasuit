package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemMekaSuitArmor.class, remap = false)
public class ItemMekaSuitArmorAbsorptionMixin {

    @Redirect(
          method = "getDamageAbsorbed",
          at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                ordinal = 0
          )
    )
    private static boolean strongermekasuit$allowTechnicalDamageForAdvancedSuit(DamageSource source, TagKey<DamageType> tag, Player player,
          DamageSource originalSource, float amount) {
        if (shouldExpandOfficialAbsorption(player, source)) {
            // 对高级套装放开 official technical 早退，让更多伤害先进入官方耗能挡伤链路。
            return false;
        }
        return source.is(tag);
    }

    @Redirect(
          method = "getDamageAbsorbed",
          at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                ordinal = 2
          )
    )
    private static boolean strongermekasuit$allowBypassDamageForAdvancedSuit(DamageSource source, TagKey<DamageType> tag, Player player,
          DamageSource originalSource, float amount) {
        if (shouldExpandOfficialAbsorption(player, source)) {
            // 对高级套装放开 official bypass early-exit，让绕甲等伤害也先接入官方挡伤。
            return false;
        }
        return source.is(tag);
    }

    private static boolean shouldExpandOfficialAbsorption(Player player, DamageSource source) {
        if (player == null || !player.isAlive() || AdvancedMekaSuitDamageHandler.getAdvancedPieceCount(player) <= 0) {
            return false;
        }
        if (AdvancedMekaSuitDamageHandler.requiresInhalationModule(source) && !AdvancedMekaSuitDamageHandler.hasEnabledInhalationModule(player)) {
            // 模块专属的魔法净化类伤害仍然保留官方门槛。
            return false;
        }
        return true;
    }
}

