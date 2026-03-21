package com.likeazusa2.strongermekasuit;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitContainerHooks;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitItem;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitModuleInterop;
import com.mojang.logging.LogUtils;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.slf4j.Logger;

@Mod(StrongerMekaSuit.MODID)
public class StrongerMekaSuit {

    public static final String MODID = "strongermekasuit";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ADVANCED_GRAVITY_ID = ResourceLocation.fromNamespaceAndPath(MODID, "advanced_gravitational_modulation");
    private static final double ADVANCED_ARMOR_ATTRIBUTE_SCALE = 2.5D;

    public StrongerMekaSuit(IEventBus modBus, ModContainer modContainer) {
        StrongerMekaSuitItems.ITEMS.register(modBus);
        StrongerMekaSuitRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
        AdvancedMekaSuitContainerHooks.register(modBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, StrongerMekaSuitConfig.SERVER_SPEC);
        modBus.addListener(AdvancedMekaSuitModuleInterop::enqueueIMC);
        modBus.addListener(StrongerMekaSuit::addCreativeTabItems);
        NeoForge.EVENT_BUS.register(this);
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.COMBAT) {
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET);
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR);
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS);
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS);
        }
    }

    @SubscribeEvent
    public void adjustAdvancedMekaSuitAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AdvancedMekaSuitItem armor)) {
            return;
        }

        // 先移除原版 MekaSuit 自带的基础属性，再补回高级套装的最终数值，避免 tooltip 重复叠词条。
        event.removeAllModifiersFor(Attributes.ARMOR);
        event.removeAllModifiersFor(Attributes.ARMOR_TOUGHNESS);
        event.removeAllModifiersFor(Attributes.KNOCKBACK_RESISTANCE);

        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(armor.getType().getSlot());
        String slotName = armor.getType().getName();
        // 每个部位都要使用独立的 modifier id，避免四件装备在属性重算时互相覆盖。
        ResourceLocation armorModifierId = ResourceLocation.fromNamespaceAndPath(MODID, "advanced_armor_" + slotName);
        ResourceLocation toughnessModifierId = ResourceLocation.fromNamespaceAndPath(MODID, "advanced_toughness_" + slotName);
        ResourceLocation knockbackModifierId = ResourceLocation.fromNamespaceAndPath(MODID, "advanced_knockback_" + slotName);
        double scaledArmor = armor.getDefense() * ADVANCED_ARMOR_ATTRIBUTE_SCALE;
        double scaledToughness = armor.getToughness() * ADVANCED_ARMOR_ATTRIBUTE_SCALE;
        double scaledKnockbackResistance = 0.1D * ADVANCED_ARMOR_ATTRIBUTE_SCALE;

        event.addModifier(Attributes.ARMOR, new AttributeModifier(armorModifierId, scaledArmor, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(toughnessModifierId, scaledToughness, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        event.addModifier(Attributes.KNOCKBACK_RESISTANCE,
              new AttributeModifier(knockbackModifierId, scaledKnockbackResistance, AttributeModifier.Operation.ADD_VALUE), slotGroup);

        if (stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR)
              && IModuleHelper.INSTANCE.isEnabled(stack, MekanismModules.GRAVITATIONAL_MODULATING_UNIT)
              && IModuleHelper.INSTANCE.getIfEnabled(stack, MekanismModules.GRAVITATIONAL_MODULATING_UNIT)
                    .hasEnoughEnergy(stack, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation)) {
            // 官方重力模块内部只认原版胸甲，这里补一个等价飞行属性给高级胸甲。
            event.addModifier(NeoForgeMod.CREATIVE_FLIGHT,
                  new AttributeModifier(ADVANCED_GRAVITY_ID, 1, AttributeModifier.Operation.ADD_VALUE),
                  EquipmentSlotGroup.CHEST);
        }
    }

    @SubscribeEvent
    public void clampAdvancedMekaSuitIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        if (!AdvancedMekaSuitDamageHandler.shouldApplyEnergyAbsorption(player, event.getSource())) {
            return;
        }

        float clampedDamage = AdvancedMekaSuitDamageHandler.clampAbnormalDamage(event.getAmount());
        if (clampedDamage < event.getAmount()) {
            // 在 NeoForge 的伤害容器层先把异常高伤压到上限，后面的原版减伤和耗电挡伤都基于这个结果继续计算。
            event.setAmount(clampedDamage);
        }
    }
}
