package com.likeazusa2.strongermekasuit.client.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuit;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.client.ClientTickHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrongerMekaSuit.MODID, value = Dist.CLIENT)
public final class AdvancedMekaSuitClientEffects {

    private static final long DEBUG_LOG_INTERVAL_TICKS = 100L;

    private AdvancedMekaSuitClientEffects() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || AdvancedMekaSuitDamageHandler.getAdvancedPieceCount(player) <= 0) {
            return;
        }
        player.hurtTime = 0;
        player.hurtDuration = 0;

        if (!player.getAbilities().instabuild && player.getAbilities().flying && player.level().getGameTime() % DEBUG_LOG_INTERVAL_TICKS == 0) {
            ItemStack chestStack = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(chestStack, 0);
            // This log is intentionally compact and periodic so we can compare hover state growth on the client.
            StrongerMekaSuit.LOGGER.info(
                  "[SMS debug][client] t={} flying={} mayfly={} creative={} gravReady={} gravOn={} pos=({},{},{}) delta=({},{},{}) chestEnergy={} overlayGroups={} overlayQuads={}",
                  player.level().getGameTime(),
                  player.getAbilities().flying,
                  player.getAbilities().mayfly,
                  player.getAbilities().instabuild,
                  CommonPlayerTickHandler.isGravitationalModulationReady(player),
                  ClientTickHandler.isGravitationalModulationOn(player),
                  format(player.getX()),
                  format(player.getY()),
                  format(player.getZ()),
                  format(player.getDeltaMovement().x),
                  format(player.getDeltaMovement().y),
                  format(player.getDeltaMovement().z),
                  energyContainer == null ? "none" : energyContainer.getEnergy() + "/" + energyContainer.getMaxEnergy(),
                  AdvancedMekaSuitModelOverlayRenderer.getCachedOverlayGroupCount(),
                  AdvancedMekaSuitModelOverlayRenderer.getCachedOverlayQuadCount()
            );
        }
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}

