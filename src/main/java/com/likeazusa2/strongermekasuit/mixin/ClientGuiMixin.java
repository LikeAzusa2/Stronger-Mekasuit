package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ClientGuiMixin {

    @Shadow
    private int lastHealth;

    @Shadow
    private int displayHealth;

    @Shadow
    private long lastHealthTime;

    @Shadow
    private long healthBlinkTime;

    @Shadow
    private int tickCount;

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void strongermekasuit$stabilizeHealthHud(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || AdvancedMekaSuitDamageHandler.getAdvancedPieceCount(player) <= 0) {
            return;
        }

        int health = (int) Math.ceil(player.getHealth());
        // Damage blink and shake are driven by these caches; keep them synchronized with real health.
        lastHealth = health;
        displayHealth = health;
        lastHealthTime = 0L;
        healthBlinkTime = tickCount;
    }
}
