package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.CrashedBombBlock;
import com.hbm.ntm.block.CrashedBombType;
import com.hbm.ntm.blockentity.CrashedBombBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBombModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/** Direct port of RenderCrashedBomb's position-seeded crash pose and model split. */
public final class CrashedBombRenderer implements BlockEntityRenderer<CrashedBombBlockEntity> {
    private static final int LEGACY_IDENTITY_MULTIPLIER = 27_644_437;

    public CrashedBombRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrashedBombBlockEntity bomb, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(bomb, getViewDistance())) {
            return;
        }
        BlockState state = bomb.getBlockState();
        if (!(state.getBlock() instanceof CrashedBombBlock)) {
            return;
        }

        Random random = new Random(legacyIdentity(bomb.getBlockPos()));
        float yaw = (float) (random.nextDouble() * 360.0D);
        float pitch = (float) (random.nextDouble() * 45.0D + 45.0D);
        float roll = (float) (random.nextDouble() * 360.0D);
        double offset = random.nextDouble() * 2.0D - 1.0D;
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(bomb, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw);
        LegacyPoseRotations.rotateXDegrees(poseStack, pitch);
        LegacyPoseRotations.rotateZDegrees(poseStack, roll);
        poseStack.translate(0.0D, 0.0D, -offset);
        renderModel(state.getValue(CrashedBombBlock.TYPE), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderModel(CrashedBombType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        LegacyWavefrontModel model = model(type);
        if (type == CrashedBombType.NUKE) {
            poseStack.translate(0.0D, 0.0D, 1.25D);
        } else if (type == CrashedBombType.SALTED) {
            poseStack.translate(0.0D, 0.0D, 0.5D);
        }
        model.renderAll(model.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public static LegacyWavefrontModel model(CrashedBombType type) {
        return switch (type) {
            case BALEFIRE -> ObjBombModels.DUD_BALEFIRE;
            case CONVENTIONAL -> ObjBombModels.DUD_CONVENTIONAL;
            case NUKE -> ObjBombModels.DUD_NUKE;
            case SALTED -> ObjBombModels.DUD_SALTED;
        };
    }

    private static int legacyIdentity(BlockPos pos) {
        return (pos.getY() + pos.getZ() * LEGACY_IDENTITY_MULTIPLIER) * LEGACY_IDENTITY_MULTIPLIER + pos.getX();
    }

    @Override
    public boolean shouldRender(CrashedBombBlockEntity bomb, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(bomb, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(bomb, getViewDistance());
    }

    @Override
    public boolean shouldRenderOffScreen(CrashedBombBlockEntity bomb) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }
}
