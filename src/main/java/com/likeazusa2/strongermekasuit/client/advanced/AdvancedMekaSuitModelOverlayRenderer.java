package com.likeazusa2.strongermekasuit.client.advanced;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.client.model.BaseModelCache;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public final class AdvancedMekaSuitModelOverlayRenderer {

    private static final QuadTransformation BASE_TRANSFORM = QuadTransformation.list(
          QuadTransformation.rotate(0, 0, 180),
          QuadTransformation.translate(-1, 0.5F, 0)
    );
    private static final RandomSource RANDOM = RandomSource.create(0);
    private static final float PURPLE_R = 0.75F;
    private static final float PURPLE_G = 0.28F;
    private static final float PURPLE_B = 1.0F;

    private static final Map<OverlayModelPos, Set<String>> HEAD_SOLID_PARTS = Map.of(
          OverlayModelPos.HEAD, Set.of(
                "helmet_head_led1",
                "helmet_head_led2",
                "helmet_head_visor_left",
                "helmet_head_visor_right",
                "helmet_head_rail1",
                "helmet_head_rail2",
                "helmet_head_rail3",
                "helmet_head_rail4"
          )
    );
    private static final Map<OverlayModelPos, Set<String>> CHEST_SOLID_PARTS = Map.of(
          OverlayModelPos.BODY, Set.of("chest_body_led1", "chest_body_led2"),
          OverlayModelPos.LEFT_ARM, Set.of("chest_left_arm_led1"),
          OverlayModelPos.RIGHT_ARM, Set.of("chest_right_arm_led1")
    );
    private static final Map<OverlayModelPos, Set<String>> LEGS_SOLID_PARTS = Map.of(
          OverlayModelPos.LEFT_LEG, Set.of(
                "leggings_left_leg_led1",
                "boots_left_leg_plate1",
                "boots_left_leg_plate2",
                "boots_left_leg_plate3"
          ),
          OverlayModelPos.RIGHT_LEG, Set.of(
                "leggings_right_leg_led2",
                "boots_right_leg_plate1",
                "boots_right_leg_plate2",
                "boots_right_leg_plate3"
          )
    );
    private static final Map<OverlayModelPos, Set<String>> CHEST_PULSE_PARTS = Map.of(
          OverlayModelPos.BODY, Set.of("chest_body_plate4", "chest_body_plate5")
    );
    private static final Map<OverlayKey, List<BakedQuad>> QUAD_CACHE = new HashMap<>();

    private AdvancedMekaSuitModelOverlayRenderer() {
    }

    public static int getCachedOverlayGroupCount() {
        return QUAD_CACHE.size();
    }

    public static int getCachedOverlayQuadCount() {
        int total = 0;
        for (List<BakedQuad> quads : QUAD_CACHE.values()) {
            total += quads.size();
        }
        return total;
    }

    public static void renderFrontOverlay(EquipmentSlot slot, HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix,
          MultiBufferSource renderer, LivingEntity entity, int overlayLight) {
        switch (slot) {
            case HEAD -> renderGroup(baseModel, matrix, renderer, overlayLight, HEAD_SOLID_PARTS, 1.0F);
            case CHEST -> {
                renderGroup(baseModel, matrix, renderer, overlayLight, CHEST_SOLID_PARTS, 1.0F);
                renderGroup(baseModel, matrix, renderer, overlayLight, CHEST_PULSE_PARTS, getPulseAlpha());
            }
            case LEGS -> renderGroup(baseModel, matrix, renderer, overlayLight, LEGS_SOLID_PARTS, 1.0F);
            default -> {
            }
        }
    }

    public static void renderArmOverlay(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer,
          int overlayLight, boolean rightHand) {
        Map<OverlayModelPos, Set<String>> parts = rightHand
              ? Map.of(OverlayModelPos.RIGHT_ARM, Set.of("chest_right_arm_led1"))
              : Map.of(OverlayModelPos.LEFT_ARM, Set.of("chest_left_arm_led1"));
        renderGroup(baseModel, matrix, renderer, overlayLight, parts, 1.0F);
    }

    private static void renderGroup(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix, MultiBufferSource renderer, int overlayLight,
          Map<OverlayModelPos, Set<String>> parts, float alpha) {
        if (parts.isEmpty() || alpha <= 0.01F) {
            return;
        }
        VertexConsumer builder = renderer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        for (Map.Entry<OverlayModelPos, Set<String>> entry : parts.entrySet()) {
            List<BakedQuad> quads = getQuads(entry.getKey(), entry.getValue());
            if (quads.isEmpty()) {
                continue;
            }
            matrix.pushPose();
            entry.getKey().translate(baseModel, matrix);
            putQuads(quads, builder, matrix.last(), overlayLight, alpha);
            matrix.popPose();
        }
    }

    private static List<BakedQuad> getQuads(OverlayModelPos pos, Set<String> parts) {
        OverlayKey key = new OverlayKey(pos, parts);
        List<BakedQuad> cached = QUAD_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        BaseModelCache.OBJModelData model = MekanismModelCache.INSTANCE.MEKASUIT;
        List<BakedQuad> raw = model.bake(new OverlayModelConfiguration(parts)).getQuads(null, null, RANDOM, ModelData.EMPTY, null);
        List<BakedQuad> transformed = raw.isEmpty() ? Collections.emptyList() : QuadUtils.transformBakedQuads(raw, pos.transform());
        QUAD_CACHE.put(key, transformed);
        return transformed;
    }

    private static void putQuads(List<BakedQuad> quads, VertexConsumer builder, PoseStack.Pose pose, int overlayLight, float alpha) {
        for (BakedQuad quad : quads) {
            builder.putBulkData(pose, quad, PURPLE_R, PURPLE_G, PURPLE_B, alpha, LightTexture.FULL_BRIGHT, overlayLight, false);
        }
    }

    private static float getPulseAlpha() {
        double t = (System.currentTimeMillis() % 2600L) / 2600D;
        double wave = (Math.sin(t * Math.PI * 2D) + 1D) * 0.5D;
        return (float) (0.30D + wave * 0.45D);
    }

    private record OverlayKey(OverlayModelPos pos, Set<String> parts) {
    }

    private enum OverlayModelPos {
        HEAD(BASE_TRANSFORM),
        BODY(BASE_TRANSFORM),
        LEFT_ARM(BASE_TRANSFORM.and(QuadTransformation.translate(-0.3125F, -0.125F, 0))),
        RIGHT_ARM(BASE_TRANSFORM.and(QuadTransformation.translate(0.3125F, -0.125F, 0))),
        LEFT_LEG(BASE_TRANSFORM.and(QuadTransformation.translate(-0.125F, -0.75F, 0))),
        RIGHT_LEG(BASE_TRANSFORM.and(QuadTransformation.translate(0.125F, -0.75F, 0)));

        private final QuadTransformation transform;

        OverlayModelPos(QuadTransformation transform) {
            this.transform = transform;
        }

        public QuadTransformation transform() {
            return transform;
        }

        public void translate(HumanoidModel<? extends LivingEntity> baseModel, PoseStack matrix) {
            switch (this) {
                case HEAD -> baseModel.head.translateAndRotate(matrix);
                case BODY -> baseModel.body.translateAndRotate(matrix);
                case LEFT_ARM -> baseModel.leftArm.translateAndRotate(matrix);
                case RIGHT_ARM -> baseModel.rightArm.translateAndRotate(matrix);
                case LEFT_LEG -> baseModel.leftLeg.translateAndRotate(matrix);
                case RIGHT_LEG -> baseModel.rightLeg.translateAndRotate(matrix);
            }
        }
    }

    private record OverlayModelConfiguration(Set<String> parts) implements IGeometryBakingContext {

        private static final Material NO_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("missingno"));

        private OverlayModelConfiguration {
            parts = parts.isEmpty() ? Collections.emptySet() : Set.copyOf(parts);
        }

        @Override
        public @NotNull String getModelName() {
            return "strongermekasuit:advanced_mekasuit_overlay";
        }

        @Override
        public boolean hasMaterial(@NotNull String name) {
            return false;
        }

        @Override
        public @NotNull Material getMaterial(@NotNull String name) {
            return NO_MATERIAL;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean useBlockLight() {
            return false;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        @Deprecated
        public @NotNull ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @Override
        public @NotNull com.mojang.math.Transformation getRootTransform() {
            return com.mojang.math.Transformation.identity();
        }

        @Override
        public @Nullable ResourceLocation getRenderTypeHint() {
            return null;
        }

        @Override
        public boolean isComponentVisible(@NotNull String component, boolean fallback) {
            return parts.contains(component);
        }
    }
}
