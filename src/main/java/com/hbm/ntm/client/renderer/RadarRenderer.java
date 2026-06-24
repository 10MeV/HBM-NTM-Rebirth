package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarLargeBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RadarRenderer<T extends RadarBlockEntity> implements BlockEntityRenderer<T> {
    private static final LegacyWavefrontModel SMALL_MODEL = ObjModelLibrary.MACHINE_RADAR_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle SMALL_BASE =
            SMALL_MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle SMALL_DISH =
            SMALL_MODEL.prepareRenderOnlyInCallOrder("Dish");
    private static final LegacyWavefrontModel LARGE_MODEL = ObjModelLibrary.MACHINE_RADAR_LARGE_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle LARGE_RADAR =
            LARGE_MODEL.prepareRenderOnlyInCallOrder("Radar");
    private static final LegacyWavefrontModel.SelectionHandle LARGE_DISH =
            LARGE_MODEL.prepareRenderOnlyInCallOrder("Dish");

    public RadarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (radar instanceof RadarLargeBlockEntity) {
            renderLarge(radar, partialTick, poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderSmall(radar, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderSmall(RadarBlockEntity radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = radar.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(radar, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        SMALL_MODEL.renderOnlyInCallOrder(context, SMALL_BASE);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(interpolatedRotation(radar, partialTick)));
        poseStack.translate(-0.125D, 0.0D, 0.0D);
        SMALL_MODEL.renderOnlyInCallOrder(context, SMALL_DISH);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderLarge(RadarBlockEntity radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = radar.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(radar, state, definition, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        LARGE_MODEL.renderOnlyInCallOrder(definition.textureLocation(), context, LARGE_RADAR);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(interpolatedRotation(radar, partialTick)));
        LARGE_MODEL.renderOnlyInCallOrder(definition.textureLocation(), context, LARGE_DISH);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static float interpolatedRotation(RadarBlockEntity radar, float partialTick) {
        return Mth.lerp(partialTick, radar.getPreviousRotation(), radar.getRotation());
    }
}
