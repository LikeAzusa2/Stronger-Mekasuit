package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class ClientGameRendererMixin {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void strongermekasuit$cancelHurtCameraBob(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && AdvancedMekaSuitDamageHandler.getAdvancedPieceCount(player) > 0) {
            ci.cancel();
        }
    }
}
