package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitDamageHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAdvancedMekaSuitDamageMixin {

    @Shadow
    public int hurtTime;

    @Shadow
    public int hurtDuration;

    @Shadow
    protected abstract void playHurtSound(DamageSource source);

    @Unique
    private DamageSource strongermekasuit$pendingDamageSource;

    @Unique
    private boolean strongermekasuit$internalSetHealth;

    @Unique
    private boolean strongermekasuit$cancelNextKnockback;

    @Unique
    private boolean strongermekasuit$cancelCurrentHurtPresentation;

    @Unique
    private boolean strongermekasuit$enteredMainDamageEntry;

    @Unique
    private boolean strongermekasuit$skipNextHurtMarked;

    @Inject(method = "hurt", at = @At("HEAD"))
    private void strongermekasuit$captureDamageContext(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        strongermekasuit$cancelCurrentHurtPresentation = false;
        strongermekasuit$enteredMainDamageEntry = false;
        strongermekasuit$skipNextHurtMarked = false;
        if (!self.level().isClientSide && self instanceof ServerPlayer player
              && AdvancedMekaSuitDamageHandler.getAdvancedPieceCount(player) > 0) {
            strongermekasuit$pendingDamageSource = source;
            strongermekasuit$cancelNextKnockback = false;
            strongermekasuit$enteredMainDamageEntry = true;
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"), cancellable = true)
    private void strongermekasuit$clearDamageContext(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (strongermekasuit$cancelCurrentHurtPresentation) {
            cir.setReturnValue(false);
        }
        if (!strongermekasuit$internalSetHealth) {
            strongermekasuit$pendingDamageSource = null;
        }
        strongermekasuit$cancelNextKnockback = false;
        strongermekasuit$cancelCurrentHurtPresentation = false;
        strongermekasuit$enteredMainDamageEntry = false;
        strongermekasuit$skipNextHurtMarked = false;
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void strongermekasuit$cancelKnockbackWhenFullyAbsorbed(double strength, double ratioX, double ratioZ, CallbackInfo ci) {
        if (strongermekasuit$cancelNextKnockback) {
            strongermekasuit$cancelNextKnockback = false;
            ci.cancel();
        }
    }

    @Redirect(
          method = "actuallyHurt",
          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V")
    )
    private void strongermekasuit$applyEnergyAbsorptionAtActualDamage(LivingEntity self, float newHealth, DamageSource source, float amount) {
        if (strongermekasuit$internalSetHealth || self.level().isClientSide || !(self instanceof ServerPlayer player)) {
            self.setHealth(newHealth);
            return;
        }

        float currentHealth = self.getHealth();
        float originalIncomingDamage = currentHealth - newHealth;
        if (originalIncomingDamage <= 0 || !AdvancedMekaSuitDamageHandler.shouldApplyEnergyAbsorption(player, source)) {
            strongermekasuit$setHealthDirectly(self, newHealth);
            return;
        }

        float incomingDamage = AdvancedMekaSuitDamageHandler.clampAbnormalDamage(player, originalIncomingDamage);

        float absorbedRatio = AdvancedMekaSuitDamageHandler.absorbRemainingDamage(player, source, incomingDamage);
        if (absorbedRatio <= 0) {
            strongermekasuit$setHealthDirectly(self, currentHealth - incomingDamage);
            return;
        }

        float remainingDamage = incomingDamage * Math.max(0, 1 - absorbedRatio);
        strongermekasuit$pendingDamageSource = null;
        if (remainingDamage <= 0) {
            strongermekasuit$clearHurtFeedback();
            strongermekasuit$cancelNextKnockback = true;
            strongermekasuit$cancelCurrentHurtPresentation = true;
            return;
        }

        strongermekasuit$skipNextHurtMarked = true;
        strongermekasuit$setHealthDirectly(self, currentHealth - remainingDamage);
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void strongermekasuit$applyEnergyAbsorptionBeforeHealthDrop(float newHealth, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (strongermekasuit$internalSetHealth || self.level().isClientSide || !(self instanceof ServerPlayer player)) {
            return;
        }

        float currentHealth = self.getHealth();
        if (newHealth >= currentHealth) {
            return;
        }

        DamageSource source = strongermekasuit$pendingDamageSource;
        if (!AdvancedMekaSuitDamageHandler.shouldApplyEnergyAbsorption(player, source)) {
            return;
        }

        float originalIncomingDamage = currentHealth - newHealth;
        float incomingDamage = originalIncomingDamage;
        if (!strongermekasuit$enteredMainDamageEntry) {
            incomingDamage = AdvancedMekaSuitDamageHandler.clampAbnormalDamage(player, incomingDamage);
        }

        float absorbedRatio = AdvancedMekaSuitDamageHandler.absorbRemainingDamage(player, source, incomingDamage);
        if (absorbedRatio <= 0) {
            if (incomingDamage < originalIncomingDamage) {
                strongermekasuit$pendingDamageSource = null;
                strongermekasuit$setHealthDirectly(self, currentHealth - incomingDamage);
                ci.cancel();
            }
            return;
        }

        float remainingDamage = incomingDamage * Math.max(0, 1 - absorbedRatio);
        strongermekasuit$pendingDamageSource = null;
        if (remainingDamage <= 0) {
            strongermekasuit$clearHurtFeedback();
            strongermekasuit$cancelNextKnockback = true;
            strongermekasuit$cancelCurrentHurtPresentation = true;
            ci.cancel();
            return;
        }

        strongermekasuit$skipNextHurtMarked = true;
        strongermekasuit$setHealthDirectly(self, currentHealth - remainingDamage);
        ci.cancel();
    }

    @Unique
    private void strongermekasuit$setHealthDirectly(LivingEntity self, float health) {
        strongermekasuit$internalSetHealth = true;
        self.setHealth(health);
        strongermekasuit$internalSetHealth = false;
    }

    @Redirect(
          method = "hurt",
          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastDamageEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V")
    )
    private void strongermekasuit$skipDamageBroadcastWhenFullyAbsorbed(Level level, net.minecraft.world.entity.Entity entity, DamageSource source) {
        if (!strongermekasuit$cancelCurrentHurtPresentation) {
            level.broadcastDamageEvent(entity, source);
        }
    }

    @Redirect(
          method = "hurt",
          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;markHurt()V")
    )
    private void strongermekasuit$skipMarkHurtWhenFullyAbsorbed(LivingEntity self) {
        if (!strongermekasuit$cancelCurrentHurtPresentation && !strongermekasuit$skipNextHurtMarked) {
            self.hurtMarked = true;
        }
    }

    @Redirect(
          method = "hurt",
          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;indicateDamage(DD)V")
    )
    private void strongermekasuit$skipDamageIndicatorWhenFullyAbsorbed(LivingEntity self, double x, double z) {
        if (!strongermekasuit$cancelCurrentHurtPresentation) {
            self.indicateDamage(x, z);
        }
    }

    @Redirect(
          method = "hurt",
          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V")
    )
    private void strongermekasuit$skipHurtSoundWhenFullyAbsorbed(LivingEntity self, DamageSource source) {
        if (!strongermekasuit$cancelCurrentHurtPresentation) {
            this.playHurtSound(source);
        }
    }

    @Unique
    private void strongermekasuit$clearHurtFeedback() {
        hurtTime = 0;
        hurtDuration = 0;
    }
}

