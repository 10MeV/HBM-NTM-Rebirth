package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.DecoCrtBlock;
import com.hbm.ntm.blockentity.DecoCrtBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Exact modern carrier for RenderCRT's two OBJ parts and its variant full-bright screen rule. */
public final class DecoCrtRenderer implements BlockEntityRenderer<DecoCrtBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjBlockModels.CRT;
    private static final LegacyWavefrontModel.SelectionHandle MONITOR = MODEL.prepareRenderOnlyInCallOrder("Monitor");
    private static final LegacyWavefrontModel.SelectionHandle SCREEN = MODEL.prepareRenderOnlyInCallOrder("Screen");

    public DecoCrtRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(DecoCrtBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(DecoCrtBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        try (var scope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            renderModel(blockEntity.getBlockState(), poseStack, buffer,
                    LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight), packedOverlay);
        }
    }

    public static void renderItemModel(BlockState state, int variant, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderModel(state.setValue(DecoCrtBlock.VARIANT, variant).setValue(DecoCrtBlock.FACING, Direction.NORTH),
                poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderModel(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        int variant = state.getValue(DecoCrtBlock.VARIANT);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, legacyYaw(state.getValue(DecoCrtBlock.FACING)));
        ResourceLocation texture = texture(variant);
        MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, MONITOR,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, variant >= 2 ? LightTexture.FULL_BRIGHT : packedLight,
                packedOverlay, SCREEN, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    private static ResourceLocation texture(int variant) {
        String name = switch (variant) {
            case 1 -> "crt_broken";
            case 2 -> "crt_blinking";
            case 3 -> "crt_bsod";
            default -> "crt_clean";
        };
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/legacy_blocks/" + name + ".png");
    }

    private static float legacyYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 0.0F;
            case SOUTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 90.0F;
        };
    }
}
