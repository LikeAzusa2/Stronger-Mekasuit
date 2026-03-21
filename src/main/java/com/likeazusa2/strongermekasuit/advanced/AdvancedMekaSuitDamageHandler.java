package com.likeazusa2.strongermekasuit.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitConfig;
import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPITags;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AdvancedMekaSuitDamageHandler {

    private AdvancedMekaSuitDamageHandler() {
    }

    public static float clampAbnormalDamage(float amount) {
        if (amount <= 0) {
            return 0;
        }
        long damageEnergyCost = MekanismConfig.gear.mekaSuitEnergyUsageDamage.get();
        if (damageEnergyCost <= 0) {
            return amount;
        }

        long requiredEnergy = MathUtils.ceilToLong(damageEnergyCost * (double) amount);
        long clampEnergy = StrongerMekaSuitConfig.SERVER.abnormalDamageClampEnergy();
        if (requiredEnergy <= clampEnergy) {
            return amount;
        }
        return (float) (clampEnergy / (double) damageEnergyCost);
    }

    public static float absorbRemainingDamage(Player player, DamageSource source, float amount) {
        if (amount <= 0 || !shouldApplyEnergyAbsorption(player, source)) {
            return 0;
        }

        float ratioAbsorbed = 0;
        List<FoundArmorDetails> armorDetails = new ArrayList<>();
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && stack.getItem() instanceof AdvancedMekaSuitItem armor) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    armorDetails.add(new FoundArmorDetails(energyContainer, armor));
                }
            }
        }

        for (FoundArmorDetails details : armorDetails) {
            float absorption = details.armor.getOfficialAbsorption();
            ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, MekanismConfig.gear.mekaSuitEnergyUsageDamage);
            if (ratioAbsorbed >= 1) {
                break;
            }
        }

        for (FoundArmorDetails details : armorDetails) {
            details.drainEnergy();
        }
        return Math.min(ratioAbsorbed, 1);
    }

    public static boolean shouldApplyEnergyAbsorption(Player player, DamageSource source) {
        if (player == null || !player.isAlive() || getAdvancedPieceCount(player) <= 0) {
            return false;
        }
        if (source != null && source.is(DamageTypes.GENERIC_KILL)) {
            return false;
        }
        if (source != null && requiresInhalationModule(source) && !hasEnabledInhalationModule(player)) {
            return false;
        }
        return true;
    }

    private static float absorbDamage(EnergyUsageInfo usageInfo, float amount, float absorption, float currentAbsorbed, LongSupplier energyCost) {
        absorption = Math.min(1 - currentAbsorbed, absorption);
        float toAbsorb = amount * absorption;
        if (toAbsorb > 0) {
            long usage = MathUtils.ceilToLong(energyCost.getAsLong() * toAbsorb);
            if (usage == 0L) {
                return absorption;
            }
            if (usageInfo.energyAvailable >= usage) {
                usageInfo.energyUsed += usage;
                usageInfo.energyAvailable -= usage;
                return absorption;
            }
            if (usageInfo.energyAvailable > 0L) {
                float absorbedPercent = (float) (usageInfo.energyAvailable / (double) usage);
                usageInfo.energyUsed += usageInfo.energyAvailable;
                usageInfo.energyAvailable = 0L;
                return absorption * absorbedPercent;
            }
        }
        return 0;
    }

    public static boolean isAdvancedMekaSuitPiece(ItemStack stack) {
        return stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET)
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR)
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS)
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS);
    }

    public static int getAdvancedPieceCount(Player player) {
        int pieces = 0;
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET)) {
            pieces++;
        }
        if (player.getItemBySlot(EquipmentSlot.CHEST).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR)) {
            pieces++;
        }
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS)) {
            pieces++;
        }
        if (player.getItemBySlot(EquipmentSlot.FEET).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS)) {
            pieces++;
        }
        return pieces;
    }

    public static boolean isFullSet(Player player) {
        return getAdvancedPieceCount(player) == 4;
    }

    public static boolean requiresInhalationModule(DamageSource source) {
        return source.is(MekanismAPITags.DamageTypes.IS_PREVENTABLE_MAGIC);
    }

    public static boolean hasEnabledInhalationModule(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return isAdvancedMekaSuitPiece(helmet)
              && IModuleHelper.INSTANCE.getIfEnabled(helmet, MekanismModules.INHALATION_PURIFICATION_UNIT) != null;
    }

    private static class FoundArmorDetails {

        private final IEnergyContainer energyContainer;
        private final EnergyUsageInfo usageInfo;
        private final AdvancedMekaSuitItem armor;

        private FoundArmorDetails(IEnergyContainer energyContainer, AdvancedMekaSuitItem armor) {
            this.energyContainer = energyContainer;
            this.usageInfo = new EnergyUsageInfo(energyContainer.getEnergy());
            this.armor = armor;
        }

        private void drainEnergy() {
            if (usageInfo.energyUsed > 0) {
                energyContainer.extract(usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    private static class EnergyUsageInfo {

        private long energyAvailable;
        private long energyUsed;

        private EnergyUsageInfo(long energyAvailable) {
            this.energyAvailable = energyAvailable;
        }
    }
}
