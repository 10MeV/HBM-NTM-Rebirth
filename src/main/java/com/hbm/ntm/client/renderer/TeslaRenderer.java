package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.TeslaBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;

public class TeslaRenderer implements BlockEntityRenderer<TeslaBlockEntity> {
    public TeslaRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(TeslaBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(TeslaBlockEntity tesla, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        ObjUtilityModels.TESLA.renderAll(ObjUtilityModels.TESLA_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        Level level = tesla.getLevel();
        if (level == null || tesla.getTargets().isEmpty()) {
            return;
        }

        List<LegacyTileRenderPlans.TeslaTargetPlan> targets = new ArrayList<>();
        for (TeslaBlockEntity.TeslaTarget target : tesla.getTargets()) {
            targets.add(new LegacyTileRenderPlans.TeslaTargetPlan(target.x(), target.y(), target.z()));
        }
        var source = tesla.sourcePosition();
        LegacyTileRenderPlans.TeslaBeamPlan plan = LegacyTileRenderPlans.teslaBeamPlan(
                source.x, source.y, source.z, targets, level.getGameTime());
        if (!plan.active()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, TeslaBlockEntity.OFFSET, 0.5D);
        for (LegacyTileRenderPlans.TeslaTargetBeamPlan beam : plan.targetBeams()) {
            LegacyBeamRenderer.beam(poseStack, buffer, beam.beam());
        }
        poseStack.popPose();
    }
}
