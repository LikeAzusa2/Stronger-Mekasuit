package com.likeazusa2.strongermekasuit.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitConfig;
import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.IModule;
import mekanism.api.math.MathUtils;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AdvancedMekaSuitDamageHandler {

    private AdvancedMekaSuitDamageHandler() {
    }

    public static float clampAbnormalDamage(Player player, float amount) {
        if (amount <= 0) {
            return 0;
        }
        FloatingLong damageEnergyCost = MekanismConfig.gear.mekaSuitEnergyUsageDamage.get();
        if (damageEnergyCost.isZero()) {
            return amount;
        }

        double clampedDamage = Math.min(amount, StrongerMekaSuitConfig.SERVER.abnormalDamageClampEnergy() / damageEnergyCost.doubleValue());
        double absorbableDamage = getFullyAbsorbableDamage(player, damageEnergyCost);
        if (absorbableDamage > 0) {
            clampedDamage = Math.min(clampedDamage, absorbableDamage);
        }
        return (float) clampedDamage;
    }

    public static float clampAbnormalDamage(float amount) {
        return clampAbnormalDamage(null, amount);
    }

    private static double getFullyAbsorbableDamage(Player player, FloatingLong damageEnergyCost) {
        List<FoundArmorDetails> armorDetails = getAdvancedArmorDetails(player);
        if (armorDetails.isEmpty()) {
            return 0;
        }

        double maxFullyAbsorbableDamage = Double.POSITIVE_INFINITY;
        float totalAbsorption = 0;
        for (FoundArmorDetails details : armorDetails) {
            float absorption = details.armor.getOfficialAbsorption();
            if (absorption <= 0) {
                continue;
            }
            totalAbsorption += absorption;
            double pieceCap = details.usageInfo.energyAvailable.divideToLevel(damageEnergyCost.multiply(absorption));
            maxFullyAbsorbableDamage = Math.min(maxFullyAbsorbableDamage, pieceCap);
        }
        if (totalAbsorption < 0.999F || !Double.isFinite(maxFullyAbsorbableDamage) || maxFullyAbsorbableDamage <= 0) {
            return 0;
        }
        return maxFullyAbsorbableDamage;
    }

    private static List<FoundArmorDetails> getAdvancedArmorDetails(Player player) {
        List<FoundArmorDetails> armorDetails = new ArrayList<>();
        if (player == null) {
            return armorDetails;
        }
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && stack.getItem() instanceof AdvancedMekaSuitItem armor) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    armorDetails.add(new FoundArmorDetails(energyContainer, armor));
                }
            }
        }
        return armorDetails;
    }

    public static float absorbRemainingDamage(Player player, DamageSource source, float amount) {
        if (amount <= 0 || !shouldApplyEnergyAbsorption(player, source)) {
            return 0;
        }

        float ratioAbsorbed = 0;
        List<FoundArmorDetails> armorDetails = getAdvancedArmorDetails(player);

        for (FoundArmorDetails details : armorDetails) {
            float absorption = details.armor.getOfficialAbsorption();
            ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, MekanismConfig.gear.mekaSuitEnergyUsageDamage.get());
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

    private static float absorbDamage(EnergyUsageInfo usageInfo, float amount, float absorption, float currentAbsorbed, FloatingLong energyCost) {
        absorption = Math.min(1 - currentAbsorbed, absorption);
        float toAbsorb = amount * absorption;
        if (toAbsorb > 0) {
            FloatingLong usage = energyCost.multiply(toAbsorb).ceil();
            if (usage.isZero()) {
                return absorption;
            }
            if (usageInfo.energyAvailable.greaterOrEqual(usage)) {
                usageInfo.energyUsed = usageInfo.energyUsed.add(usage);
                usageInfo.energyAvailable = usageInfo.energyAvailable.subtract(usage);
                return absorption;
            }
            if (!usageInfo.energyAvailable.isZero()) {
                float absorbedPercent = (float) usageInfo.energyAvailable.divideToLevel(usage);
                usageInfo.energyUsed = usageInfo.energyUsed.add(usageInfo.energyAvailable);
                usageInfo.energyAvailable = FloatingLong.ZERO;
                return absorption * absorbedPercent;
            }
        }
        return 0;
    }

    public static boolean isAdvancedMekaSuitPiece(ItemStack stack) {
        return stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get())
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get())
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get())
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get());
    }

    public static int getAdvancedPieceCount(Player player) {
        int pieces = 0;
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get())) {
            pieces++;
        }
        if (player.getItemBySlot(EquipmentSlot.CHEST).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get())) {
            pieces++;
        }
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get())) {
            pieces++;
        }
        if (player.getItemBySlot(EquipmentSlot.FEET).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get())) {
            pieces++;
        }
        return pieces;
    }

    public static boolean isFullSet(Player player) {
        return getAdvancedPieceCount(player) == 4;
    }

    public static boolean requiresInhalationModule(DamageSource source) {
        return source.is(MekanismTags.DamageTypes.IS_PREVENTABLE_MAGIC);
    }

    public static boolean hasEnabledInhalationModule(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        IModule<?> module = isAdvancedMekaSuitPiece(helmet) ? IModuleHelper.INSTANCE.load(helmet, MekanismModules.INHALATION_PURIFICATION_UNIT) : null;
        return module != null && module.isEnabled();
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
            if (!usageInfo.energyUsed.isZero()) {
                energyContainer.extract(usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    private static class EnergyUsageInfo {

        private FloatingLong energyAvailable;
        private FloatingLong energyUsed;

        private EnergyUsageInfo(FloatingLong energyAvailable) {
            this.energyAvailable = energyAvailable;
            this.energyUsed = FloatingLong.ZERO;
        }
    }
}

