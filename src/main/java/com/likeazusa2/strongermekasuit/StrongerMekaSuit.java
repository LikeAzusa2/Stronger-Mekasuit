package com.likeazusa2.strongermekasuit;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitContainerHooks;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitItem;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitModuleInterop;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.StorageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
@Mod(StrongerMekaSuit.MODID)
public class StrongerMekaSuit {

    public static final String MODID = "strongermekasuit";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final double ADVANCED_ARMOR_ATTRIBUTE_SCALE = 2.5D;
    private static final long DEBUG_LOG_INTERVAL_TICKS = 100L;

    @SuppressWarnings("removal")
    public StrongerMekaSuit() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        StrongerMekaSuitItems.ITEMS.register(modBus);
        StrongerMekaSuitRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
        AdvancedMekaSuitContainerHooks.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, StrongerMekaSuitConfig.SERVER_SPEC);
        modBus.addListener(AdvancedMekaSuitModuleInterop::enqueueIMC);
        modBus.addListener(StrongerMekaSuit::addCreativeTabItems);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get());
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get());
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get());
            event.accept(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get());
        }
    }

    @SubscribeEvent
    public void adjustAdvancedMekaSuitAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AdvancedMekaSuitItem armor)) {
            return;
        }
        if (event.getSlotType() != armor.getEquipmentSlot()) {
            return;
        }

        event.removeAttribute(Attributes.ARMOR);
        event.removeAttribute(Attributes.ARMOR_TOUGHNESS);
        event.removeAttribute(Attributes.KNOCKBACK_RESISTANCE);

        EquipmentSlot slot = event.getSlotType();
        String slotName = armor.getType().getName();
        double scaledArmor = armor.getDefense() * ADVANCED_ARMOR_ATTRIBUTE_SCALE;
        double scaledToughness = armor.getToughness() * ADVANCED_ARMOR_ATTRIBUTE_SCALE;
        double scaledKnockbackResistance = 0.1D * ADVANCED_ARMOR_ATTRIBUTE_SCALE;

        event.addModifier(Attributes.ARMOR,
              new AttributeModifier(stableModifierId("armor", slotName), "advanced_armor_" + slotName,
                    scaledArmor, AttributeModifier.Operation.ADDITION));
        event.addModifier(Attributes.ARMOR_TOUGHNESS,
              new AttributeModifier(stableModifierId("toughness", slotName), "advanced_toughness_" + slotName,
                    scaledToughness, AttributeModifier.Operation.ADDITION));
        event.addModifier(Attributes.KNOCKBACK_RESISTANCE,
              new AttributeModifier(stableModifierId("knockback", slotName), "advanced_knockback_" + slotName,
                    scaledKnockbackResistance,
                    AttributeModifier.Operation.ADDITION));
    }

    /**
     * ItemAttributeModifierEvent is evaluated again whenever an equipped stack's NBT changes. The UUID must
     * therefore be stable across every evaluation so LivingEntity can remove the previous modifier instance.
     */
    private static UUID stableModifierId(String attribute, String slotName) {
        return UUID.nameUUIDFromBytes((MODID + ":advanced_" + attribute + ":" + slotName).getBytes(StandardCharsets.UTF_8));
    }

    @SubscribeEvent
    public void logAdvancedFlightState(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (AdvancedMekaSuitDamageHandler.getAdvancedPieceCount(player) <= 0 || player.getAbilities().instabuild || !player.getAbilities().flying) {
            return;
        }
        if (player.level().getGameTime() % DEBUG_LOG_INTERVAL_TICKS != 0) {
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(chestStack, 0);
        // Server-side samples help distinguish integrated-server tick stalls from client-only rendering stalls.
        LOGGER.info(
              "[SMS debug][server] t={} flying={} mayfly={} gravReady={} pos=({},{},{}) delta=({},{},{}) chestEnergy={}",
              player.level().getGameTime(),
              player.getAbilities().flying,
              player.getAbilities().mayfly,
              CommonPlayerTickHandler.isGravitationalModulationReady(player),
              format(player.getX()),
              format(player.getY()),
              format(player.getZ()),
              format(player.getDeltaMovement().x),
              format(player.getDeltaMovement().y),
              format(player.getDeltaMovement().z),
              energyContainer == null ? "none" : energyContainer.getEnergy() + "/" + energyContainer.getMaxEnergy()
        );
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}

