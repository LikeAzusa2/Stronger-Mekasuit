package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;
import java.util.Optional;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleChargeDistributionUnit;
import mekanism.common.integration.curios.CuriosIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModuleChargeDistributionUnit.class, remap = false)
public abstract class ModuleChargeDistributionUnitMixin {

    private static final long BATCH_INTERVAL_TICKS = 5L;

    @Shadow
    private IModuleConfigItem<Boolean> chargeSuit;

    @Shadow
    private IModuleConfigItem<Boolean> chargeInventory;

    @Shadow
    private void chargeSuit(Player player) {
    }

    @Shadow
    protected abstract FloatingLong charge(IModule<ModuleChargeDistributionUnit> module, Player player, ItemStack stack, FloatingLong amount);

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void strongermekasuit$batchAdvancedChargeDistribution(IModule<ModuleChargeDistributionUnit> module, Player player, CallbackInfo ci) {
        if (!module.getContainer().is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get())) {
            return;
        }
        ci.cancel();
        // Charging inventory items every tick causes constant stack-NBT churn and client slot updates.
        // Batch the work for advanced armor so charging stays functionally the same without hammering sync.
        if (player.level().getGameTime() % BATCH_INTERVAL_TICKS != 0) {
            return;
        }
        if (chargeInventory.get()) {
            chargeInventoryBatched(module, player);
        }
        if (chargeSuit.get()) {
            chargeSuit(player);
        }
    }

    private void chargeInventoryBatched(IModule<ModuleChargeDistributionUnit> module, Player player) {
        FloatingLong toCharge = MekanismConfig.gear.mekaSuitInventoryChargeRate.get().multiply(BATCH_INTERVAL_TICKS);
        toCharge = charge(module, player, player.getMainHandItem(), toCharge);
        toCharge = charge(module, player, player.getOffhandItem(), toCharge);
        if (!toCharge.isZero()) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack != player.getMainHandItem() && stack != player.getOffhandItem()) {
                    toCharge = charge(module, player, stack, toCharge);
                    if (toCharge.isZero()) {
                        break;
                    }
                }
            }
            if (!toCharge.isZero() && Mekanism.hooks.CuriosLoaded) {
                Optional<? extends IItemHandler> curiosInventory = CuriosIntegration.getCuriosInventory(player);
                if (curiosInventory.isPresent()) {
                    IItemHandler handler = curiosInventory.get();
                    for (int slot = 0, slots = handler.getSlots(); slot < slots; slot++) {
                        toCharge = charge(module, player, handler.getStackInSlot(slot), toCharge);
                        if (toCharge.isZero()) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
