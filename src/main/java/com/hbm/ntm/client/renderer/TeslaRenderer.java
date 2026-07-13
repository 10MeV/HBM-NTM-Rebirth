package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.TeslaBlockEntity;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TeslaRenderer implements BlockEntityRenderer<TeslaBlockEntity> {
    public TeslaRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(TeslaBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(TeslaBlockEntity blockEntity, Vec3 cameraPos) {
        return blockEntity.hasTargets()
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(TeslaBlockEntity tesla, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tesla.getLevel();
        if (level == null || !tesla.hasTargets()) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(tesla, getViewDistance())) {
            return;
        }
        List<TeslaBlockEntity.TeslaTarget> targetView = tesla.getTargets();
        if (targetView.isEmpty()) {
            return;
        }

        double sourceX = tesla.sourceX();
        double sourceY = tesla.sourceY();
        double sourceZ = tesla.sourceZ();
        int start = (int) (level.getGameTime() % 1000L) + 1;

        poseStack.pushPose();
        poseStack.translate(0.5D, TeslaBlockEntity.OFFSET, 0.5D);
        LegacyMachineEffectPresenter.enqueueTeslaTargetBeams(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                targetView, sourceX, sourceY, sourceZ, start, LegacyTileRenderPlans.TESLA_BEAM_COLOR,
                LegacyTileRenderPlans.TESLA_BEAM_SIZE, LegacyTileRenderPlans.TESLA_BEAM_LAYERS,
                LegacyTileRenderPlans.TESLA_BEAM_THICKNESS);
        poseStack.popPose();
    }
}
