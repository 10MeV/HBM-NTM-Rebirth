package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.MissilePartItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class CustomMissileItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation FALLBACK_ICON =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/item/missile_custom.png");
    private static final float GUI_TARGET_SIZE = 0.9F;
    private static final float WORLD_TARGET_SIZE = 0.68F;

    public static final CustomMissileItemRenderer INSTANCE = new CustomMissileItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private CustomMissileItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        CustomMissileParts parts = CustomMissileParts.fromStack(stack);
        if (parts == null || !parts.hasRenderablePart()) {
            renderFallbackIcon(displayContext, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack, parts);
        ObjMissilePartModels.renderMissile(parts.thruster(), parts.fins(), parts.fuselage(), parts.warhead(),
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack, CustomMissileParts parts) {
        double height = parts.height();
        if (height <= 0.0D) {
            height = 4.0D;
        }
        AABB bounds = parts.bounds();
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(height, Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize())));
        float targetSize = displayContext == ItemDisplayContext.GUI ? GUI_TARGET_SIZE : WORLD_TARGET_SIZE;
        float fitScale = (float) Math.max(0.03D, Math.min(0.42D, targetSize / Math.max(1.0D, maxSize)));

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(135.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(215.0F));
            poseStack.mulPose(Axis.YN.rotationDegrees((System.currentTimeMillis() / 25L) % 360L));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.scale(0.8F, 0.8F, 0.8F);
            } else if (displayContext.firstPerson()) {
                poseStack.translate(0.0D, 0.1D, 0.0D);
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
        }
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(-fitScale, -fitScale, -fitScale);
        } else {
            poseStack.scale(fitScale, fitScale, fitScale);
        }
        poseStack.translate(-center.x, -center.y, -center.z);
    }

    private static void renderFallbackIcon(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.0D, 0.0D, 0.5D);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(-0.5D, -0.5D, 0.0D);
        }

        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(FALLBACK_ICON));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        vertex(vertices, matrix, normal, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
        vertex(vertices, matrix, normal, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        vertex(vertices, matrix, normal, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay);
        vertex(vertices, matrix, normal, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Matrix3f normal, float x, float y, float z,
            float u, float v, int packedLight, int packedOverlay) {
        vertices.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    private static AABB include(AABB bounds, ObjMissilePartModels.LegacyMissilePart part, double yOffset) {
        if (part == null) {
            return bounds;
        }
        AABB partBounds = part.model().boundsAll().move(0.0D, yOffset, 0.0D);
        return bounds == null ? partBounds : bounds.minmax(partBounds);
    }

    @Nullable
    private static ObjMissilePartModels.LegacyMissilePart part(ItemStack stack, String tagKey,
            ObjMissilePartModels.PartKind expectedKind) {
        ResourceLocation id = CustomMissileItem.getPartId(stack, tagKey);
        if (id == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (!(item instanceof MissilePartItem missilePart)) {
            return null;
        }
        ObjMissilePartModels.LegacyMissilePart part = ObjMissilePartModels.part(missilePart.legacyModelKey());
        return part != null && part.kind() == expectedKind ? part : null;
    }

    private record CustomMissileParts(
            ObjMissilePartModels.LegacyMissilePart warhead,
            ObjMissilePartModels.LegacyMissilePart fuselage,
            ObjMissilePartModels.LegacyMissilePart fins,
            ObjMissilePartModels.LegacyMissilePart thruster) {
        @Nullable
        static CustomMissileParts fromStack(ItemStack stack) {
            if (!(stack.getItem() instanceof CustomMissileItem)) {
                return null;
            }
            return new CustomMissileParts(
                    part(stack, CustomMissileItem.TAG_WARHEAD, ObjMissilePartModels.PartKind.WARHEAD),
                    part(stack, CustomMissileItem.TAG_FUSELAGE, ObjMissilePartModels.PartKind.FUSELAGE),
                    part(stack, CustomMissileItem.TAG_STABILITY, ObjMissilePartModels.PartKind.FINS),
                    part(stack, CustomMissileItem.TAG_THRUSTER, ObjMissilePartModels.PartKind.THRUSTER));
        }

        boolean hasRenderablePart() {
            return warhead != null || fuselage != null || fins != null || thruster != null;
        }

        double height() {
            return ObjMissilePartModels.missileHeight(thruster, fuselage, warhead);
        }

        AABB bounds() {
            AABB bounds = null;
            double y = 0.0D;
            if (thruster != null) {
                bounds = include(bounds, thruster, y);
                y += thruster.height();
            }
            if (fuselage != null) {
                if (fins != null) {
                    bounds = include(bounds, fins, y);
                }
                bounds = include(bounds, fuselage, y);
                y += fuselage.height();
            }
            if (warhead != null) {
                bounds = include(bounds, warhead, y);
            }
            return bounds == null ? new AABB(0.0D, 0.0D, 0.0D, 1.0D, 4.0D, 1.0D) : bounds;
        }
    }
}
