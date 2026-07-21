package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyRailSwitchBlock;
import com.hbm.ntm.blockentity.RailSwitchBlockEntity;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Renders exactly one legacy switch sign group after the static Rail group. */
public final class RailSwitchRenderer implements BlockEntityRenderer<RailSwitchBlockEntity> {
    private static final ResourceLocation LEFT_SIGN = texture("rail_switch_sign");
    private static final ResourceLocation RIGHT_SIGN = texture("rail_switch_sign_flipped");

    public RailSwitchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(RailSwitchBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(RailSwitchBlockEntity switchEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = switchEntity.getBlockState();
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(switchEntity, getViewDistance())
                || !(state.getBlock() instanceof LegacyRailSwitchBlock switchBlock)) {
            return;
        }
        boolean flipped = switchBlock.variant() == LegacyRailSwitchBlock.Variant.RIGHT;
        poseStack.pushPose();
        applyLegacyWorldTransform(poseStack, state.getValue(LegacyRailSwitchBlock.FACING));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(switchEntity)) {
            (flipped ? ObjBlockModels.RAIL_STANDARD_SWITCH_FLIPPED : ObjBlockModels.RAIL_STANDARD_SWITCH)
                    .renderPart(switchEntity.isSwitched() ? "SignTurn" : "SignStraight", flipped ? RIGHT_SIGN : LEFT_SIGN,
                            poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void applyLegacyWorldTransform(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case NORTH -> {
                poseStack.translate(1.0D, 0.0D, 0.5D);
                LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            }
            case SOUTH -> poseStack.translate(0.0D, 0.0D, 0.5D);
            case EAST -> {
                poseStack.translate(0.5D, 0.0D, 1.0D);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            }
            case WEST -> {
                poseStack.translate(0.5D, 0.0D, 0.0D);
                LegacyPoseRotations.rotateYDegrees(poseStack, 270.0F);
            }
            default -> throw new IllegalArgumentException("Rail switch facing must be horizontal: " + facing);
        }
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/legacy_blocks/" + name + ".png");
    }
}
