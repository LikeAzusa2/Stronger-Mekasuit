package com.likeazusa2.strongermekasuit.client.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuit;
import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = StrongerMekaSuit.MODID, value = Dist.CLIENT)
public final class AdvancedMekaSuitClientEffects {

    private AdvancedMekaSuitClientEffects() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || AdvancedMekaSuitDamageHandler.getAdvancedPieceCount(player) <= 0) {
            return;
        }

        // 直接清掉本地玩家的受击计时，统一压掉受击抖动、模型抖腿等依赖 hurtTime 的表现。
        player.hurtTime = 0;
        player.hurtDuration = 0;
    }
}
