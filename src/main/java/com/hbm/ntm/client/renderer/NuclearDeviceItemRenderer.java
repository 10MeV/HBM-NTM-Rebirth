package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.block.CustomNukeBlock;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjNukeModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NuclearDeviceItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;

    public static final NuclearDeviceItemRenderer INSTANCE = new NuclearDeviceItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private NuclearDeviceItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        if (blockItem.getBlock() instanceof CustomNukeBlock) {
            renderCustomNuke(displayContext, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        if (!(blockItem.getBlock() instanceof NuclearDeviceBlock block)) {
            return;
        }

        NuclearDeviceBlock.Kind kind = block.kind();
        LegacyWavefrontModel model = NuclearDeviceRenderer.model(kind);
        boolean gui = displayContext == ItemDisplayContext.GUI;
        AABB bounds = gui ? transformedInventoryBounds(kind, model.boundsAll()) : transformedCommonBounds(kind, model.boundsAll());
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        poseStack.pushPose();
        applyBaseDisplay(displayContext, poseStack, center, maxSize, gui);
        if (gui) {
            applyLegacyInventory(kind, poseStack);
        }
        applyLegacyCommon(kind, poseStack);
        NuclearDeviceRenderer.renderKind(kind, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderCustomNuke(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel model = ObjNukeModels.BOY;
        boolean gui = displayContext == ItemDisplayContext.GUI;
        AABB bounds = customNukeBounds(model.boundsAll(), gui);
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        poseStack.pushPose();
        applyBaseDisplay(displayContext, poseStack, center, maxSize, gui);
        if (gui) {
            poseStack.scale(5.0F, 5.0F, 5.0F);
        }
        NuclearDeviceRenderer.applyCustomNukeLegacyItemCommon(poseStack);
        NuclearDeviceRenderer.renderCustomNuke(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyBaseDisplay(ItemDisplayContext displayContext, PoseStack poseStack, Vec3 center,
            double maxSize, boolean gui) {
        float fitScale = (float) Math.max(0.035D, Math.min(0.32D,
                targetDisplaySize(gui, maxSize) / Math.max(1.0D, maxSize)));

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(fitScale, fitScale, fitScale);
            poseStack.translate(-center.x, -center.y, -center.z);
            return;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float worldScale = fitScale * 0.82F;
        poseStack.scale(worldScale, worldScale, worldScale);
        poseStack.translate(-center.x, -center.y, -center.z);

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
        } else if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, 0.1D, 0.0D);
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }
    }

    private static double targetDisplaySize(boolean gui, double maxSize) {
        if (gui) {
            return Math.min(LEGACY_GUI_MAX_OCCUPANCY, maxSize / LEGACY_GUI_SLOT_PIXELS);
        }
        return LEGACY_GUI_MAX_OCCUPANCY;
    }

    private static void applyLegacyCommon(NuclearDeviceBlock.Kind kind, PoseStack poseStack) {
        switch (kind) {
            case GADGET -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case BOY -> poseStack.translate(-1.0D, 0.0D, 0.0D);
            case MAN -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.translate(-0.75D, 0.0D, 0.0D);
            }
            case TSAR -> poseStack.translate(1.5D, 0.0D, 0.0D);
            case PROTOTYPE -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(0.0D, 0.125D, 0.0D);
            }
            case FLEIJA -> {
                poseStack.translate(0.125D, 0.0D, 0.0D);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case SOLINIUM -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(0.0D, -0.125D, 0.0D);
            }
            case MIKE, N2 -> {
            }
        }
    }

    private static void applyLegacyInventory(NuclearDeviceBlock.Kind kind, PoseStack poseStack) {
        poseStack.translate(0.0D, legacyInventoryTranslationY(kind), 0.0D);
        float scale = legacyInventoryScale(kind);
        poseStack.scale(scale, scale, scale);
    }

    private static AABB transformedInventoryBounds(NuclearDeviceBlock.Kind kind, AABB bounds) {
        return transformedLegacyBounds(kind, bounds, legacyInventoryScale(kind), 0.0D,
                legacyInventoryTranslationY(kind), 0.0D);
    }

    private static AABB transformedCommonBounds(NuclearDeviceBlock.Kind kind, AABB bounds) {
        return transformedLegacyBounds(kind, bounds, 1.0D, 0.0D, 0.0D, 0.0D);
    }

    private static double legacyInventoryTranslationY(NuclearDeviceBlock.Kind kind) {
        return switch (kind) {
            case GADGET -> -3.0D;
            case MAN -> -2.0D;
            case MIKE, N2 -> -5.0D;
            case PROTOTYPE -> 0.125D;
            case SOLINIUM -> -0.125D;
            default -> 0.0D;
        };
    }

    private static float legacyInventoryScale(NuclearDeviceBlock.Kind kind) {
        return switch (kind) {
            case GADGET, MAN, SOLINIUM -> 5.0F;
            case PROTOTYPE -> 3.0F;
            case MIKE, TSAR, N2 -> 2.25F;
            case FLEIJA -> 6.8F;
            case BOY -> 5.0F;
        };
    }

    private static AABB customNukeBounds(AABB bounds, boolean gui) {
        double scale = gui ? 5.0D : 1.0D;
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> accumulator.include(
                (x - 1.0D) * scale, y * scale, z * scale));
    }

    private static AABB transformedLegacyBounds(NuclearDeviceBlock.Kind kind, AABB bounds, double scale,
            double translateX, double translateY, double translateZ) {
        return switch (kind) {
            case GADGET -> transformedRotateY(bounds, -90.0F, 0.0D, 0.0D, 0.0D,
                    0.0D, 0.0D, 0.0D, scale, translateX, translateY, translateZ);
            case BOY -> transformedTranslate(bounds, -1.0D, 0.0D, 0.0D, scale, translateX, translateY, translateZ);
            case MAN -> transformedRotateY(bounds, 180.0F, -0.75D, 0.0D, 0.0D,
                    0.0D, 0.0D, 0.0D, scale, translateX, translateY, translateZ);
            case TSAR -> transformedTranslate(bounds, 1.5D, 0.0D, 0.0D, scale, translateX, translateY, translateZ);
            case PROTOTYPE -> transformedRotateY(bounds, 90.0F, 0.0D, 0.125D, 0.0D,
                    0.0D, 0.0D, 0.0D, scale, translateX, translateY, translateZ);
            case FLEIJA -> transformedRotateY(bounds, 90.0F, 0.0D, 0.0D, 0.0D,
                    0.125D, 0.0D, 0.0D, scale, translateX, translateY, translateZ);
            case SOLINIUM -> transformedRotateY(bounds, 90.0F, 0.0D, -0.125D, 0.0D,
                    0.0D, 0.0D, 0.0D, scale, translateX, translateY, translateZ);
            case MIKE, N2 -> transformedTranslate(bounds, 0.0D, 0.0D, 0.0D, scale,
                    translateX, translateY, translateZ);
        };
    }

    private static AABB transformedTranslate(AABB bounds, double addX, double addY, double addZ, double scale,
            double translateX, double translateY, double translateZ) {
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> accumulator.include(
                (x + addX) * scale + translateX,
                (y + addY) * scale + translateY,
                (z + addZ) * scale + translateZ));
    }

    private static AABB transformedRotateY(AABB bounds, float degrees, double preX, double preY, double preZ,
            double postX, double postY, double postZ, double scale, double translateX, double translateY,
            double translateZ) {
        double sin = LegacyTransformedBounds.sinDeg(degrees);
        double cos = LegacyTransformedBounds.cosDeg(degrees);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double rotatedX = LegacyTransformedBounds.rotateYX(x + preX, z + preZ, sin, cos) + postX;
            double rotatedY = y + preY + postY;
            double rotatedZ = LegacyTransformedBounds.rotateYZ(x + preX, z + preZ, sin, cos) + postZ;
            accumulator.include(rotatedX * scale + translateX,
                    rotatedY * scale + translateY,
                    rotatedZ * scale + translateZ);
        });
    }
}
