package com.likeazusa2.strongermekasuit.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.energy.AttachedEnergy;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyContainer;
import mekanism.common.attachments.containers.energy.ComponentBackedNoClampEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.attachments.containers.fluid.FluidTanksBuilder;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismModules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class AdvancedMekaSuitContainerHooks {

    private static final long BASE_ENERGY_CAPACITY_MULTIPLIER = 64L;
    private static final long BASE_CHARGE_RATE_MULTIPLIER = 64L;

    private AdvancedMekaSuitContainerHooks() {
    }

    public static void register(IEventBus modBus) {
        // 延后到 common setup 再补容器，避免物品实例还没注册完成时提前读取。
        modBus.addListener((FMLCommonSetupEvent event) -> registerContainers(modBus));
    }

    private static void registerContainers(IEventBus modBus) {
        registerArmor(modBus, StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get());
        registerArmor(modBus, StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get());
        registerArmor(modBus, StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get());
        registerArmor(modBus, StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get());
    }

    private static void registerArmor(IEventBus modBus, AdvancedMekaSuitItem item) {
        registerScaledSupportTanks(modBus, item);
        modBus.addListener(item::attachCapabilities);

        // 只放大基础能量相关参数，继续复用官方 Energy Unit 的成长公式。
        ContainerType.ENERGY.addDefaultCreators(modBus, item, AdvancedMekaSuitContainerHooks::createAdvancedMekaSuitEnergy, MekanismConfig.gear);
    }

    private static void registerScaledSupportTanks(IEventBus modBus, AdvancedMekaSuitItem item) {
        switch (item.getType()) {
            case HELMET -> ContainerType.FLUID.addDefaultCreators(modBus, item, () -> FluidTanksBuilder.builder().addTank(
                  (type, attachedTo, containerIndex) -> new mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank(
                        attachedTo,
                        containerIndex,
                        mekanism.api.functions.ConstantPredicates.notExternal(),
                        (fluid, automationType) -> hasNutritionalModule(attachedTo),
                        fluid -> fluid.is(MekanismFluids.NUTRITIONAL_PASTE),
                        AdvancedMekaSuitContainerHooks::getScaledNutritionalTransferRate,
                        AdvancedMekaSuitContainerHooks::getScaledNutritionalCapacity
                  )
            ).build(), MekanismConfig.gear);
            case CHESTPLATE -> ContainerType.CHEMICAL.addDefaultCreators(modBus, item, () -> ChemicalTanksBuilder.builder().addTank(
                  (type, attachedTo, containerIndex) -> new mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank(
                        attachedTo,
                        containerIndex,
                        mekanism.api.functions.ConstantPredicates.notExternal(),
                        (chemical, automationType) -> hasJetpackModule(attachedTo),
                        chemical -> chemical.is(MekanismChemicals.HYDROGEN),
                        AdvancedMekaSuitContainerHooks::getScaledJetpackTransferRate,
                        () -> getScaledJetpackCapacity(attachedTo),
                        null
                  )
            ).build(), MekanismConfig.gear);
            default -> {
            }
        }
    }

    private static BaseContainerCreator<AttachedEnergy, ComponentBackedEnergyContainer> createAdvancedMekaSuitEnergy() {
        return EnergyContainersBuilder.builder().addContainer((type, attachedTo, containerIndex) -> new ComponentBackedNoClampEnergyContainer(
              attachedTo,
              containerIndex,
              BasicEnergyContainer.manualOnly,
              automationType -> true,
              () -> ModuleEnergyUnit.getChargeRate(attachedTo, AdvancedMekaSuitContainerHooks::getScaledBaseChargeRate),
              () -> ModuleEnergyUnit.getEnergyCapacity(attachedTo, AdvancedMekaSuitContainerHooks::getScaledBaseEnergyCapacity)
        )).build();
    }

    private static boolean hasNutritionalModule(net.minecraft.world.item.ItemStack stack) {
        return IModuleHelper.INSTANCE.getModule(stack, MekanismModules.NUTRITIONAL_INJECTION_UNIT) != null;
    }

    private static boolean hasJetpackModule(net.minecraft.world.item.ItemStack stack) {
        return IModuleHelper.INSTANCE.getModule(stack, MekanismModules.JETPACK_UNIT) != null;
    }

    private static int getScaledNutritionalCapacity() {
        return MathUtils.clampToInt(MekanismConfig.gear.mekaSuitNutritionalMaxStorage.get() * 4L);
    }

    private static int getScaledNutritionalTransferRate() {
        return MathUtils.clampToInt(MekanismConfig.gear.mekaSuitNutritionalTransferRate.get() * 4L);
    }

    private static long getScaledJetpackCapacity(net.minecraft.world.item.ItemStack stack) {
        IModule<ModuleJetpackUnit> module = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.JETPACK_UNIT);
        if (module == null) {
            return 0L;
        }
        return MathUtils.clampToLong(MekanismConfig.gear.mekaSuitJetpackMaxStorage.get() * 4D * module.getInstalledCount());
    }

    private static long getScaledJetpackTransferRate() {
        return MathUtils.clampToLong(MekanismConfig.gear.mekaSuitJetpackTransferRate.get() * 4L);
    }

    private static long getScaledBaseEnergyCapacity() {
        return MathUtils.clampToLong(MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get() * BASE_ENERGY_CAPACITY_MULTIPLIER);
    }

    private static long getScaledBaseChargeRate() {
        return MathUtils.clampToLong(MekanismConfig.gear.mekaSuitBaseChargeRate.get() * BASE_CHARGE_RATE_MULTIPLIER);
    }
}
