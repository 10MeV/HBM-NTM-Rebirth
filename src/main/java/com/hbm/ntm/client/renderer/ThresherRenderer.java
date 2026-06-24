package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.ThresherBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ThresherRenderer implements BlockEntityRenderer<ThresherBlockEntity> {
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            ObjMachineModels.THRESHER.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle ENGINE =
            ObjMachineModels.THRESHER.prepareRenderOnlyInCallOrder("Engine");
    private static final LegacyWavefrontModel.SelectionHandle ARM_UPPER =
            ObjMachineModels.THRESHER.prepareRenderOnlyInCallOrder("ArmUpper");
    private static final LegacyWavefrontModel.SelectionHandle ARM_LOWER =
            ObjMachineModels.THRESHER.prepareRenderOnlyInCallOrder("ArmLower");
    private static final LegacyWavefrontModel.SelectionHandle FRONT =
            ObjMachineModels.THRESHER.prepareRenderOnlyInCallOrder("Front");
    private static final LegacyWavefrontModel.SelectionHandle WHEEL =
            ObjMachineModels.THRESHER.prepareRenderOnlyInCallOrder("Wheel");

    public ThresherRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ThresherBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ThresherBlockEntity thresher, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = thresher.getBlockState();
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation(state)));
        LegacyTileRenderPlans.ThresherPlan plan = LegacyTileRenderPlans.thresherPlan(
                thresher.getPreviousAngle(), thresher.getAngle(),
                thresher.getLastSpin(), thresher.getSpin(),
                thresher.isOn(), worldTime(thresher), partialTick);
        renderPart(BASE, context);
        poseStack.translate(0.0D, plan.engineTranslateY(), 0.0D);
        renderPart(ENGINE, context);
        poseStack.translate(0.0D, -plan.engineTranslateY(), 0.0D);
        renderPart(plan.armUpper(), ARM_UPPER, context, poseStack);
        renderPart(plan.armLower(), ARM_LOWER, context, poseStack);
        renderPart(plan.front(), FRONT, context, poseStack);
        renderPart(plan.wheel(), WHEEL, context, poseStack);
        poseStack.popPose();
    }

    private static void renderPart(LegacyTileRenderPlans.PivotedModelPartPlan part,
            LegacyWavefrontModel.SelectionHandle handle, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        if (part.axisX() != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (part.angleDegrees() * part.axisX())));
        }
        if (part.axisY() != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (part.angleDegrees() * part.axisY())));
        }
        if (part.axisZ() != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (part.angleDegrees() * part.axisZ())));
        }
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderPart(handle, context);
        poseStack.popPose();
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, ObjRenderContext context) {
        ObjMachineModels.THRESHER.renderOnlyInCallOrder(ObjMachineModels.THRESHER_TEXTURE, context, handle);
    }

    static void renderModelPart(String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            renderPart(handle, context);
            return;
        }
        ObjMachineModels.THRESHER.renderPart(partName, ObjMachineModels.THRESHER_TEXTURE, context);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Engine" -> ENGINE;
            case "ArmUpper" -> ARM_UPPER;
            case "ArmLower" -> ARM_LOWER;
            case "Front" -> FRONT;
            case "Wheel" -> WHEEL;
            default -> null;
        };
    }

    private static float rotation(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static long worldTime(ThresherBlockEntity thresher) {
        Level level = thresher.getLevel();
        return level == null ? 0L : level.getGameTime();
    }
}
