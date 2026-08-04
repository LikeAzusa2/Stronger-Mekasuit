package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;
import java.util.UUID;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.client.sound.SoundHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SoundHandler.class, remap = false)
public abstract class SoundHandlerAdvancedFlightMixin {

    @Inject(method = "startSound", at = @At("HEAD"), cancellable = true)
    private static void strongermekasuit$skipAdvancedGravityFlightSound(LevelAccessor world, UUID uuid, SoundType soundType, CallbackInfo ci) {
        if (soundType != SoundType.GRAVITATIONAL_MODULATOR) {
            return;
        }
        Player player = world.getPlayerByUUID(uuid);
        // Survival-only flight sound is one of the few client paths that differs from creative flight.
        // Skip it for the advanced chest so we can avoid the hover-time slowdown without touching flight logic.
        if (player != null && player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get())) {
            ci.cancel();
        }
    }
}
