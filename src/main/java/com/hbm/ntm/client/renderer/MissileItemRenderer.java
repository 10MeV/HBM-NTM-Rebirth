package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MissileItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float GUI_TARGET_SIZE = 0.86F;
    private static final float WORLD_TARGET_SIZE = 0.72F;

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
            return new RenderSpec(ObjMissilePartModels.MISSILE_MICRO,
                    ObjMissilePartModels.MISSILE_MICRO_TEST_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_TAINT.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_MICRO,
                    ObjMissilePartModels.MISSILE_MICRO_TAINT_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_MICRO.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_MICRO,
                    ObjMissilePartModels.MISSILE_MICRO_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_BHOLE.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_MICRO,
                    ObjMissilePartModels.MISSILE_MICRO_BHOLE_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_SCHRABIDIUM.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_MICRO,
                    ObjMissilePartModels.MISSILE_MICRO_SCHRAB_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_EMP.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_MICRO,
                    ObjMissilePartModels.MISSILE_MICRO_EMP_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_STEALTH.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_STEALTH,
                    ObjMissilePartModels.MISSILE_STEALTH_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_GENERIC.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_V2,
                    ObjMissilePartModels.MISSILE_V2_HE_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_INCENDIARY.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_V2,
                    ObjMissilePartModels.MISSILE_V2_IN_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_CLUSTER.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_V2,
                    ObjMissilePartModels.MISSILE_V2_CL_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_BUSTER.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_V2,
                    ObjMissilePartModels.MISSILE_V2_BU_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_DECOY.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_V2,
                    ObjMissilePartModels.MISSILE_V2_DECOY_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_ANTI_BALLISTIC.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_ABM,
                    ObjMissilePartModels.MISSILE_ABM_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_STRONG.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_STRONG,
                    ObjMissilePartModels.MISSILE_STRONG_HE_TEXTURE, 1.5F);
        }
        if (stack.is(ModItems.MISSILE_INCENDIARY_STRONG.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_STRONG,
                    ObjMissilePartModels.MISSILE_STRONG_IN_TEXTURE, 1.5F);
        }
        if (stack.is(ModItems.MISSILE_CLUSTER_STRONG.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_STRONG,
                    ObjMissilePartModels.MISSILE_STRONG_CL_TEXTURE, 1.5F);
        }
        if (stack.is(ModItems.MISSILE_BUSTER_STRONG.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_STRONG,
                    ObjMissilePartModels.MISSILE_STRONG_BU_TEXTURE, 1.5F);
        }
        if (stack.is(ModItems.MISSILE_EMP_STRONG.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_STRONG,
                    ObjMissilePartModels.MISSILE_STRONG_EMP_TEXTURE, 1.5F);
        }
        if (stack.is(ModItems.MISSILE_BURST.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_HUGE,
                    ObjMissilePartModels.MISSILE_HUGE_HE_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_INFERNO.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_HUGE,
                    ObjMissilePartModels.MISSILE_HUGE_IN_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_RAIN.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_HUGE,
                    ObjMissilePartModels.MISSILE_HUGE_CL_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_DRILL.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_HUGE,
                    ObjMissilePartModels.MISSILE_HUGE_BU_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_NUCLEAR.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_ATLAS,
                    ObjMissilePartModels.MISSILE_ATLAS_NUCLEAR_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_NUCLEAR_CLUSTER.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_ATLAS,
                    ObjMissilePartModels.MISSILE_ATLAS_THERMO_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_VOLCANO.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_ATLAS,
                    ObjMissilePartModels.MISSILE_ATLAS_VOLCANO_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_DOOMSDAY.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_ATLAS,
                    ObjMissilePartModels.MISSILE_ATLAS_DOOMSDAY_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_DOOMSDAY_RUSTED.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_ATLAS,
                    ObjMissilePartModels.MISSILE_ATLAS_DOOMSDAY_RUSTED_TEXTURE, 1.0F);
        }
        if (stack.is(ModItems.MISSILE_SHUTTLE.get())) {
            return new RenderSpec(ObjMissilePartModels.MISSILE_SHUTTLE,
                    ObjMissilePartModels.MISSILE_SHUTTLE_TEXTURE, 1.0F);
        }
        return null;
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack, RenderSpec spec) {
        AABB bounds = scaledBounds(spec.model().boundsAll(), spec.modelScale());
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float targetSize = displayContext == ItemDisplayContext.GUI ? GUI_TARGET_SIZE : WORLD_TARGET_SIZE;
        float fitScale = (float) Math.max(0.025D, Math.min(0.45D, targetSize / Math.max(1.0D, maxSize)));

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(135.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees((System.currentTimeMillis() / 15L) % 360L));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.scale(0.8F, 0.8F, 0.8F);
            } else if (displayContext.firstPerson()) {
                poseStack.translate(0.0D, 0.1D, 0.0D);
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
        }
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-center.x / spec.modelScale(), -center.y / spec.modelScale(), -center.z / spec.modelScale());
    }

    private static AABB scaledBounds(AABB bounds, float scale) {
        return new AABB(
                bounds.minX * scale,
                bounds.minY * scale,
                bounds.minZ * scale,
                bounds.maxX * scale,
                bounds.maxY * scale,
                bounds.maxZ * scale);
    }

    private record RenderSpec(LegacyWavefrontModel model, ResourceLocation texture, float modelScale) {
    }
}
