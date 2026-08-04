package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.common.base.PlayerState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlayerState.class, remap = false)
public abstract class PlayerStateAdvancedFlightMixin {

    private static final long BATCH_INTERVAL_TICKS = 5L;

    @Redirect(
          method = "updateFlightInfo",
          at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/player/Player;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;)V"
          )
    )
    private void strongermekasuit$skipGravityVibrationsForAdvancedSuit(Player player, GameEvent event) {
        // Advanced flight already has enough feedback. Suppress Mekanism's vibration emission so underground
        // hover does not keep waking nearby game-event listeners and dragging singleplayer performance down.
        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get())) {
            player.gameEvent(event);
        }
    }

    @Redirect(
          method = "updateFlightInfo",
          at = @At(
                value = "INVOKE",
                target = "Lmekanism/api/gear/IModule;useEnergy(Lnet/minecraft/world/entity/LivingEntity;Lmekanism/api/math/FloatingLong;)Lmekanism/api/math/FloatingLong;"
          )
    )
    private FloatingLong strongermekasuit$batchAdvancedFlightEnergyUse(IModule<?> module, LivingEntity wearer, FloatingLong energy) {
        if (wearer instanceof Player player && player.getItemBySlot(EquipmentSlot.CHEST).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get())) {
            // Flight drains energy every tick by default, which means the chest stack NBT changes every tick as well.
            // Batch the extraction so the client receives fewer slot updates while preserving the same average energy cost.
            if (player.level().getGameTime() % BATCH_INTERVAL_TICKS != 0) {
                return FloatingLong.ZERO;
            }
            return module.useEnergy(wearer, energy.multiply(BATCH_INTERVAL_TICKS));
        }
        return module.useEnergy(wearer, energy);
    }
}
