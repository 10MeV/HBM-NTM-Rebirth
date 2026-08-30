package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class MissileItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float GUI_TARGET_SIZE = 0.86F;
    private static final float WORLD_TARGET_SIZE = 0.72F;
    private static final RenderSpec MISSILE_TEST_SPEC = spec(ObjMissilePartModels.MISSILE_MICRO,
            ObjMissilePartModels.MISSILE_MICRO_TEST_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_TAINT_SPEC = spec(ObjMissilePartModels.MISSILE_MICRO,
            ObjMissilePartModels.MISSILE_MICRO_TAINT_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_MICRO_SPEC = spec(ObjMissilePartModels.MISSILE_MICRO,
            ObjMissilePartModels.MISSILE_MICRO_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_BHOLE_SPEC = spec(ObjMissilePartModels.MISSILE_MICRO,
            ObjMissilePartModels.MISSILE_MICRO_BHOLE_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_SCHRABIDIUM_SPEC = spec(ObjMissilePartModels.MISSILE_MICRO,
            ObjMissilePartModels.MISSILE_MICRO_SCHRAB_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_EMP_SPEC = spec(ObjMissilePartModels.MISSILE_MICRO,
            ObjMissilePartModels.MISSILE_MICRO_EMP_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_STEALTH_SPEC = spec(ObjMissilePartModels.MISSILE_STEALTH,
            ObjMissilePartModels.MISSILE_STEALTH_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_GENERIC_SPEC = spec(ObjMissilePartModels.MISSILE_V2,
            ObjMissilePartModels.MISSILE_V2_HE_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_INCENDIARY_SPEC = spec(ObjMissilePartModels.MISSILE_V2,
            ObjMissilePartModels.MISSILE_V2_IN_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_CLUSTER_SPEC = spec(ObjMissilePartModels.MISSILE_V2,
            ObjMissilePartModels.MISSILE_V2_CL_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_BUSTER_SPEC = spec(ObjMissilePartModels.MISSILE_V2,
            ObjMissilePartModels.MISSILE_V2_BU_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_DECOY_SPEC = spec(ObjMissilePartModels.MISSILE_V2,
            ObjMissilePartModels.MISSILE_V2_DECOY_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_ANTI_BALLISTIC_SPEC = spec(ObjMissilePartModels.MISSILE_ABM,
            ObjMissilePartModels.MISSILE_ABM_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_STRONG_SPEC = spec(ObjMissilePartModels.MISSILE_STRONG,
            ObjMissilePartModels.MISSILE_STRONG_HE_TEXTURE, 1.5F);
    private static final RenderSpec MISSILE_INCENDIARY_STRONG_SPEC = spec(ObjMissilePartModels.MISSILE_STRONG,
            ObjMissilePartModels.MISSILE_STRONG_IN_TEXTURE, 1.5F);
    private static final RenderSpec MISSILE_CLUSTER_STRONG_SPEC = spec(ObjMissilePartModels.MISSILE_STRONG,
            ObjMissilePartModels.MISSILE_STRONG_CL_TEXTURE, 1.5F);
    private static final RenderSpec MISSILE_BUSTER_STRONG_SPEC = spec(ObjMissilePartModels.MISSILE_STRONG,
            ObjMissilePartModels.MISSILE_STRONG_BU_TEXTURE, 1.5F);
    private static final RenderSpec MISSILE_EMP_STRONG_SPEC = spec(ObjMissilePartModels.MISSILE_STRONG,
            ObjMissilePartModels.MISSILE_STRONG_EMP_TEXTURE, 1.5F);
    private static final RenderSpec MISSILE_BURST_SPEC = spec(ObjMissilePartModels.MISSILE_HUGE,
            ObjMissilePartModels.MISSILE_HUGE_HE_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_INFERNO_SPEC = spec(ObjMissilePartModels.MISSILE_HUGE,
            ObjMissilePartModels.MISSILE_HUGE_IN_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_RAIN_SPEC = spec(ObjMissilePartModels.MISSILE_HUGE,
            ObjMissilePartModels.MISSILE_HUGE_CL_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_DRILL_SPEC = spec(ObjMissilePartModels.MISSILE_HUGE,
            ObjMissilePartModels.MISSILE_HUGE_BU_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_NUCLEAR_SPEC = spec(ObjMissilePartModels.MISSILE_ATLAS,
            ObjMissilePartModels.MISSILE_ATLAS_NUCLEAR_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_NUCLEAR_CLUSTER_SPEC = spec(ObjMissilePartModels.MISSILE_ATLAS,
            ObjMissilePartModels.MISSILE_ATLAS_THERMO_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_VOLCANO_SPEC = spec(ObjMissilePartModels.MISSILE_ATLAS,
            ObjMissilePartModels.MISSILE_ATLAS_VOLCANO_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_DOOMSDAY_SPEC = spec(ObjMissilePartModels.MISSILE_ATLAS,
            ObjMissilePartModels.MISSILE_ATLAS_DOOMSDAY_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_DOOMSDAY_RUSTED_SPEC = spec(ObjMissilePartModels.MISSILE_ATLAS,
            ObjMissilePartModels.MISSILE_ATLAS_DOOMSDAY_RUSTED_TEXTURE, 1.0F);
    private static final RenderSpec MISSILE_SHUTTLE_SPEC = spec(ObjMissilePartModels.MISSILE_SHUTTLE,
            ObjMissilePartModels.MISSILE_SHUTTLE_TEXTURE, 1.0F);

    public static final MissileItemRenderer INSTANCE = new MissileItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private MissileItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RenderSpec spec = specFor(stack);
        if (spec == null) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack, spec);
        poseStack.scale(spec.modelScale(), spec.modelScale(), spec.modelScale());
        spec.model().renderAll(spec.texture(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static boolean renderRawMissile(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        RenderSpec spec = specFor(stack);
        if (spec == null) {
            return false;
        }
        poseStack.scale(spec.modelScale(), spec.modelScale(), spec.modelScale());
        spec.model().renderAll(spec.texture(), poseStack, buffer, packedLight, packedOverlay);
        return true;
    }

    private static RenderSpec specFor(ItemStack stack) {
        if (stack.is(ModItems.MISSILE_TEST.get())) {
            return MISSILE_TEST_SPEC;
        }
        if (stack.is(ModItems.MISSILE_TAINT.get())) {
            return MISSILE_TAINT_SPEC;
        }
        if (stack.is(ModItems.MISSILE_MICRO.get())) {
            return MISSILE_MICRO_SPEC;
        }
        if (stack.is(ModItems.MISSILE_BHOLE.get())) {
            return MISSILE_BHOLE_SPEC;
        }
        if (stack.is(ModItems.MISSILE_SCHRABIDIUM.get())) {
            return MISSILE_SCHRABIDIUM_SPEC;
        }
        if (stack.is(ModItems.MISSILE_EMP.get())) {
            return MISSILE_EMP_SPEC;
        }
        if (stack.is(ModItems.MISSILE_STEALTH.get())) {
            return MISSILE_STEALTH_SPEC;
        }
        if (stack.is(ModItems.MISSILE_GENERIC.get())) {
            return MISSILE_GENERIC_SPEC;
        }
        if (stack.is(ModItems.MISSILE_INCENDIARY.get())) {
            return MISSILE_INCENDIARY_SPEC;
        }
        if (stack.is(ModItems.MISSILE_CLUSTER.get())) {
            return MISSILE_CLUSTER_SPEC;
        }
        if (stack.is(ModItems.MISSILE_BUSTER.get())) {
            return MISSILE_BUSTER_SPEC;
        }
        if (stack.is(ModItems.MISSILE_DECOY.get())) {
            return MISSILE_DECOY_SPEC;
        }
        if (stack.is(ModItems.MISSILE_ANTI_BALLISTIC.get())) {
            return MISSILE_ANTI_BALLISTIC_SPEC;
        }
        if (stack.is(ModItems.MISSILE_STRONG.get())) {
            return MISSILE_STRONG_SPEC;
        }
        if (stack.is(ModItems.MISSILE_INCENDIARY_STRONG.get())) {
            return MISSILE_INCENDIARY_STRONG_SPEC;
        }
        if (stack.is(ModItems.MISSILE_CLUSTER_STRONG.get())) {
            return MISSILE_CLUSTER_STRONG_SPEC;
        }
        if (stack.is(ModItems.MISSILE_BUSTER_STRONG.get())) {
            return MISSILE_BUSTER_STRONG_SPEC;
        }
        if (stack.is(ModItems.MISSILE_EMP_STRONG.get())) {
            return MISSILE_EMP_STRONG_SPEC;
        }
        if (stack.is(ModItems.MISSILE_BURST.get())) {
            return MISSILE_BURST_SPEC;
        }
        if (stack.is(ModItems.MISSILE_INFERNO.get())) {
            return MISSILE_INFERNO_SPEC;
        }
        if (stack.is(ModItems.MISSILE_RAIN.get())) {
            return MISSILE_RAIN_SPEC;
        }
        if (stack.is(ModItems.MISSILE_DRILL.get())) {
            return MISSILE_DRILL_SPEC;
        }
        if (stack.is(ModItems.MISSILE_NUCLEAR.get())) {
            return MISSILE_NUCLEAR_SPEC;
        }
        if (stack.is(ModItems.MISSILE_NUCLEAR_CLUSTER.get())) {
            return MISSILE_NUCLEAR_CLUSTER_SPEC;
        }
        if (stack.is(ModItems.MISSILE_VOLCANO.get())) {
            return MISSILE_VOLCANO_SPEC;
        }
        if (stack.is(ModItems.MISSILE_DOOMSDAY.get())) {
            return MISSILE_DOOMSDAY_SPEC;
        }
        if (stack.is(ModItems.MISSILE_DOOMSDAY_RUSTED.get())) {
            return MISSILE_DOOMSDAY_RUSTED_SPEC;
        }
        if (stack.is(ModItems.MISSILE_SHUTTLE.get())) {
            return MISSILE_SHUTTLE_SPEC;
        }
        return null;
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack, RenderSpec spec) {
        float fitScale = displayContext == ItemDisplayContext.GUI ? spec.guiFitScale() : spec.worldFitScale();

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            // GUI inherited coordinates mirror the apparent Z direction: this is the requested clockwise 90° from 135°.
            LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, (System.currentTimeMillis() / 15L) % 360L);
        } else {
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.scale(0.8F, 0.8F, 0.8F);
            } else if (displayContext.firstPerson()) {
                poseStack.translate(0.0D, 0.1D, 0.0D);
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
        }
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
    }

    private static RenderSpec spec(LegacyWavefrontModel model, ResourceLocation texture, float modelScale) {
        AABB bounds = model.boundsAll();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize())) * modelScale;
        RenderSpec spec = new RenderSpec(model, texture, modelScale,
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                fitScale(GUI_TARGET_SIZE, maxSize),
                fitScale(WORLD_TARGET_SIZE, maxSize));
        return spec;
    }

    private static float fitScale(float targetSize, double maxSize) {
        return (float) Math.max(0.025D, Math.min(0.45D, targetSize / Math.max(1.0D, maxSize)));
    }

    private record RenderSpec(
            LegacyWavefrontModel model,
            ResourceLocation texture,
            float modelScale,
            double centerX,
            double centerY,
            double centerZ,
            float guiFitScale,
            float worldFitScale) {
    }
}
