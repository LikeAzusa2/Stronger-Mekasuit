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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// remap stays off: every selector here names only Mekanism-owned methods, fields and types, which never
// get renamed by reobfuscation, so official and SRG names agree in both dev and production. Color values
// are swapped via getstatic Color.WHITE redirects (a Mekanism target with no Minecraft-class descriptor)
// rather than @ModifyArg on putQuads/render, whose INVOKE descriptors contain SRG-renamed Minecraft types
// that would otherwise require a refmap and fail against the production Mekanism jar.
@Mixin(value = MekaSuitArmor.class, remap = false)
public abstract class MekaSuitArmorTransparentLayerMixin {

    private static final ThreadLocal<Boolean> ADVANCED_RENDER_CONTEXT = ThreadLocal.withInitial(() -> false);

    @Shadow
    @Final
    private EquipmentSlot type;

    @Inject(method = "renderArm", at = @At("HEAD"), require = 0)
    private void strongermekasuit$captureArmContext(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, LivingEntity entity, ItemStack stack, boolean rightHand, CallbackInfo ci) {
        ADVANCED_RENDER_CONTEXT.set(StrongerMekaSuitItems.isAdvancedPiece(stack));
    }

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void strongermekasuit$captureRenderContext(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        ADVANCED_RENDER_CONTEXT.set(StrongerMekaSuitItems.isAdvancedPiece(stack));
    }

    @Inject(method = "renderArm", at = @At("RETURN"), require = 0)
    private void strongermekasuit$clearArmContext(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, LivingEntity entity, ItemStack stack, boolean rightHand, CallbackInfo ci) {
        ADVANCED_RENDER_CONTEXT.remove();
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
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
          ),
          require = 0
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
          ),
          require = 0
    )
    private Set<?> strongermekasuit$filterModuleModelsSecondPass(MekanismModelCache cache) {
        return filterModuleModels(cache.MEKASUIT_MODULES);
    }

    // The transparent pass of renderArm pulls its tint from getstatic Color.WHITE. Redirecting that
    // single field read is equivalent to the old @ModifyArg on the second putQuads call,
    // but stays remap-safe because the target references no Minecraft class. GETSTATIC redirects
    // take no handler arguments, so the non-advanced path must return Color.WHITE directly.
    @Redirect(
          method = "renderArm",
          at = @At(
                value = "FIELD",
                target = "Lmekanism/common/lib/Color;WHITE:Lmekanism/common/lib/Color;",
                opcode = Opcodes.GETSTATIC
          ),
          require = 0
    )
    private Color strongermekasuit$modifyTransparentColorInArm() {
        return ADVANCED_RENDER_CONTEXT.get() ? getPulsingPurple() : Color.WHITE;
    }

    // Same idea for renderMekaSuit's second (transparent) pass, which reads Color.WHITE before
    // invoking the private render overload.
    @Redirect(
          method = "renderMekaSuit",
          at = @At(
                value = "FIELD",
                target = "Lmekanism/common/lib/Color;WHITE:Lmekanism/common/lib/Color;",
                opcode = Opcodes.GETSTATIC
          ),
          require = 0
    )
    private Color strongermekasuit$modifyTransparentColorInArmor() {
        return ADVANCED_RENDER_CONTEXT.get() ? getPulsingPurple() : Color.WHITE;
    }

    @Inject(method = "renderArm", at = @At("RETURN"), require = 0)
    private void strongermekasuit$renderAdvancedArmOverlay(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, LivingEntity entity, ItemStack stack, boolean rightHand, CallbackInfo ci) {
        if (StrongerMekaSuitItems.isAdvancedPiece(stack)) {
            AdvancedMekaSuitModelOverlayRenderer.renderArmOverlay(baseModel, matrix, renderer, overlayLight, rightHand);
        }
    }

    @Inject(method = "renderMekaSuit", at = @At("RETURN"), require = 0)
    private void strongermekasuit$renderAdvancedOverlay(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, Color color, float partialTicks, boolean hasEffect, LivingEntity entity, CallbackInfo ci) {
        ItemStack worn = entity.getItemBySlot(type);
        if (StrongerMekaSuitItems.isAdvancedPiece(worn)) {
            AdvancedMekaSuitModelOverlayRenderer.renderFrontOverlay(type, baseModel, matrix, renderer, entity, overlayLight);
        }
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
        return filtered == null ? models : java.util.Collections.unmodifiableSet(filtered);
    }

    private static boolean isAdvancedElytraOverrideModel(Object model) {
        if (!(model instanceof MekaSuitArmor.ModuleOBJModelData moduleModelData) || moduleModelData.getModel() == null) {
            return false;
        }
        Set<String> rootNames = moduleModelData.getModel().getRootComponentNames();
        return !rootNames.isEmpty() && rootNames.stream().allMatch(name -> name.startsWith("override_elytra_"));
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

