package com.likeazusa2.strongermekasuit.mixin;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitItems;
import com.likeazusa2.strongermekasuit.client.advanced.AdvancedMekaSuitModelOverlayRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashSet;
import java.util.Set;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.common.lib.Color;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MekaSuitArmor.class, remap = false)
public abstract class MekaSuitArmorTransparentLayerMixin {

    private static final ThreadLocal<Boolean> ADVANCED_RENDER_CONTEXT = ThreadLocal.withInitial(() -> false);

    @Shadow
    @Final
    private EquipmentSlot type;

    @Inject(method = "renderArm", at = @At("HEAD"))
    private void strongermekasuit$captureArmContext(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, LivingEntity entity, ItemStack stack, boolean rightHand, CallbackInfo ci) {
        ADVANCED_RENDER_CONTEXT.set(isAdvancedPiece(stack));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void strongermekasuit$captureRenderContext(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        ADVANCED_RENDER_CONTEXT.set(isAdvancedPiece(stack));
    }

    @Inject(method = "renderArm", at = @At("RETURN"))
    private void strongermekasuit$clearArmContext(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, LivingEntity entity, ItemStack stack, boolean rightHand, CallbackInfo ci) {
        ADVANCED_RENDER_CONTEXT.remove();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void strongermekasuit$clearRenderContext(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        ADVANCED_RENDER_CONTEXT.remove();
    }

    @Redirect(
          method = "createQuads",
          at = @At(
                value = "FIELD",
                target = "Lmekanism/client/model/MekanismModelCache;MEKASUIT_MODULES:Ljava/util/Set;",
                opcode = Opcodes.GETFIELD,
                ordinal = 0
          )
    )
    private Set<?> strongermekasuit$filterModuleModelsFirstPass(MekanismModelCache cache) {
        return filterModuleModels(cache.MEKASUIT_MODULES);
    }

    @Redirect(
          method = "createQuads",
          at = @At(
                value = "FIELD",
                target = "Lmekanism/client/model/MekanismModelCache;MEKASUIT_MODULES:Ljava/util/Set;",
                opcode = Opcodes.GETFIELD,
                ordinal = 1
          )
    )
    private Set<?> strongermekasuit$filterModuleModelsSecondPass(MekanismModelCache cache) {
        return filterModuleModels(cache.MEKASUIT_MODULES);
    }

    @ModifyArg(
          method = "renderArm",
          at = @At(
                value = "INVOKE",
                target = "Lmekanism/client/render/armor/MekaSuitArmor;putQuads(Ljava/util/List;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;IILmekanism/common/lib/Color;)V",
                ordinal = 1
          ),
          index = 5
    )
    private Color strongermekasuit$modifyTransparentColorInArm(Color original) {
        return ADVANCED_RENDER_CONTEXT.get() ? getPulsingPurple() : original;
    }

    @ModifyArg(
          method = "renderMekaSuit",
          at = @At(
                value = "INVOKE",
                target = "Lmekanism/client/render/armor/MekaSuitArmor;render(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IILmekanism/common/lib/Color;ZLnet/minecraft/world/entity/LivingEntity;Ljava/util/Map;Z)V",
                ordinal = 1
          ),
          index = 5
    )
    private Color strongermekasuit$modifyTransparentColorInArmor(Color original) {
        return ADVANCED_RENDER_CONTEXT.get() ? getPulsingPurple() : original;
    }

    @Inject(method = "renderArm", at = @At("RETURN"))
    private void strongermekasuit$renderAdvancedArmOverlay(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, LivingEntity entity, ItemStack stack, boolean rightHand, CallbackInfo ci) {
        if (isAdvancedPiece(stack)) {
            AdvancedMekaSuitModelOverlayRenderer.renderArmOverlay(baseModel, matrix, renderer, overlayLight, rightHand);
        }
    }

    @Inject(method = "renderMekaSuit", at = @At("RETURN"))
    private void strongermekasuit$renderAdvancedOverlay(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, Color color, float partialTicks, boolean hasEffect, LivingEntity entity, CallbackInfo ci) {
        ItemStack worn = entity.getItemBySlot(type);
        if (isAdvancedPiece(worn)) {
            AdvancedMekaSuitModelOverlayRenderer.renderFrontOverlay(type, baseModel, matrix, renderer, entity, overlayLight);
        }
    }

    private static boolean isAdvancedPiece(ItemStack stack) {
        return stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_HELMET.get())
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BODYARMOR.get())
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_PANTS.get())
              || stack.is(StrongerMekaSuitItems.ADVANCED_MEKASUIT_BOOTS.get());
    }

    private static Set<?> filterModuleModels(Set<?> models) {
        if (ADVANCED_RENDER_CONTEXT.get()) {
            return models;
        }
        // The advanced elytra override model is registered globally with Mekanism so it can participate
        // in the normal module render pipeline. Strip it back out for vanilla MekaSuit renders so the
        // purple wing assets stay exclusive to the advanced armor set.
        Set<Object> filtered = null;
        for (Object model : models) {
            if (isAdvancedElytraOverrideModel(model)) {
                if (filtered == null) {
                    filtered = new LinkedHashSet<>(models);
                }
                filtered.remove(model);
            }
        }
        return filtered == null ? models : Set.copyOf(filtered);
    }

    private static boolean isAdvancedElytraOverrideModel(Object model) {
        if (!(model instanceof MekaSuitArmor.ModuleOBJModelData moduleModelData) || moduleModelData.getModel() == null) {
            return false;
        }
        return moduleModelData.getModel().getRootComponentNames().stream().allMatch(name -> name.startsWith("override_elytra_"));
    }

    private static Color getPulsingPurple() {
        double t = (System.currentTimeMillis() % 2200L) / 2200D;
        double pulse = (Math.sin(t * Math.PI * 2D) + 1D) * 0.5D;
        double r = 0.75D + 0.20D * pulse;
        double g = 0.18D + 0.08D * pulse;
        double b = 0.95D;
        double a = 0.85D + 0.15D * pulse;
        return Color.rgbad(r, g, b, a);
    }
}

