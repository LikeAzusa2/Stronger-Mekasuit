package com.likeazusa2.strongermekasuit.advanced;

import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.chemical.item.RateLimitMultiTankGasHandler;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler.FluidTankSpec;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismModules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedMekaSuitItem extends ItemMekaSuitArmor {

    private static final long BASE_ENERGY_CAPACITY_MULTIPLIER = 64L;
    private static final long BASE_CHARGE_RATE_MULTIPLIER = 64L;
    private static final int SUPPORT_TANK_MULTIPLIER = 4;

    public AdvancedMekaSuitItem(ArmorItem.Type armorType, Item.Properties properties) {
        super(armorType, properties);
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        capabilities.removeIf(capability -> capability instanceof RateLimitEnergyHandler
              || capability instanceof RateLimitMultiTankGasHandler
              || capability instanceof RateLimitMultiTankFluidHandler);

        capabilities.add(RateLimitEnergyHandler.create(
              () -> getScaledChargeRate(stack),
              () -> getScaledEnergyCapacity(stack),
              mekanism.common.capabilities.energy.BasicEnergyContainer.manualOnly,
              mekanism.common.capabilities.energy.BasicEnergyContainer.alwaysTrue
        ));

        if (getType() == ArmorItem.Type.CHESTPLATE) {
            capabilities.add(RateLimitMultiTankGasHandler.create(List.of(
                  ChemicalTankSpec.createFillOnly(
                        this::getScaledJetpackTransferRate,
                        this::getScaledJetpackCapacity,
                        gas -> gas == MekanismGases.HYDROGEN.getChemical(),
                        this::hasJetpackModule
                  )
            )));
        } else if (getType() == ArmorItem.Type.HELMET) {
            capabilities.add(RateLimitMultiTankFluidHandler.create(List.of(
                  FluidTankSpec.createFillOnly(
                        this::getScaledNutritionalTransferRate,
                        this::getScaledNutritionalCapacity,
                        fluid -> fluid.getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid(),
                        this::hasNutritionalModule
                  )
            )));
        }
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return stack.getMaxStackSize() == 1;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    public float getBypassArmorAbsorption() {
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

    private FloatingLong getScaledEnergyCapacity(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = IModuleHelper.INSTANCE.load(stack, MekanismModules.ENERGY_UNIT);
        if (module == null) {
            return FloatingLong.ZERO;
        }
        return module.getCustomInstance().getEnergyCapacity(module).multiply(BASE_ENERGY_CAPACITY_MULTIPLIER);
    }

    private FloatingLong getScaledChargeRate(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = IModuleHelper.INSTANCE.load(stack, MekanismModules.ENERGY_UNIT);
        if (module == null) {
            return FloatingLong.ZERO;
        }
        return module.getCustomInstance().getChargeRate(module).multiply(BASE_CHARGE_RATE_MULTIPLIER);
    }

    private int getScaledNutritionalCapacity() {
        return MekanismConfig.gear.mekaSuitNutritionalMaxStorage.get() * SUPPORT_TANK_MULTIPLIER;
    }

    private int getScaledNutritionalTransferRate() {
        return MekanismConfig.gear.mekaSuitNutritionalTransferRate.get() * SUPPORT_TANK_MULTIPLIER;
    }

    private long getScaledJetpackCapacity() {
        return MekanismConfig.gear.mekaSuitJetpackMaxStorage.get() * SUPPORT_TANK_MULTIPLIER;
    }

    private long getScaledJetpackTransferRate() {
        return MekanismConfig.gear.mekaSuitJetpackTransferRate.get() * SUPPORT_TANK_MULTIPLIER;
    }

    private boolean hasNutritionalModule(ItemStack stack) {
        return IModuleHelper.INSTANCE.load(stack, MekanismModules.NUTRITIONAL_INJECTION_UNIT) != null;
    }

    private boolean hasJetpackModule(ItemStack stack) {
        return IModuleHelper.INSTANCE.load(stack, MekanismModules.JETPACK_UNIT) != null;
    }
}
