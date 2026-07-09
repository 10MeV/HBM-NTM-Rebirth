package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.api.entity.RadarScreenDisplayProfile;
import com.hbm.ntm.api.entity.RadarScreenSnapshot;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RadarScreenRenderer implements BlockEntityRenderer<RadarScreenBlockEntity> {
    private static final ResourceLocation RADAR_GUI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_nt.png");

    public RadarScreenRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(RadarScreenBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(RadarScreenBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return RadarScreenDisplayProfile.VIEW_DISTANCE;
    }

    @Override
    public void render(RadarScreenBlockEntity screen, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(screen, getViewDistance())) {
            return;
        }
        BlockState state = screen.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            int modelLight = LegacyRenderLighting.resolveMachineLight(screen, state, definition, packedLight);
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(screen)) {
                ObjModelLibrary.MACHINE_RADAR_SCREEN_LEGACY.renderAll(definition.textureLocation(),
                        poseStack, buffer, modelLight, packedOverlay);
            }
        }

        RadarScreenSnapshot snapshot = screen.getSnapshot();
        Level level = screen.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        RadarScreenDisplayProfile.WorldOverlay overlay =
                RadarScreenDisplayProfile.overlay(snapshot, gameTime, partialTick, screen.getBlockPos());
        if (overlay.linked()) {
            enqueueLinkedOverlay(overlay, poseStack, buffer, packedOverlay);
        } else {
            enqueueNoiseOverlay(overlay, poseStack, buffer, packedOverlay);
        }

        poseStack.popPose();
    }

    private static void enqueueLinkedOverlay(RadarScreenDisplayProfile.WorldOverlay overlay, PoseStack poseStack,
            MultiBufferSource buffer, int packedOverlay) {
        LegacyMachineEffectPresenter.enqueueUntexturedQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, 0,
                group -> LegacyRadarDisplayRenderer.emitWorldLinkedSweep(group, overlay.sweepOffset()));

        RadarScreenSnapshot snapshot = overlay.snapshot();
        if (snapshot == null || snapshot.entries().isEmpty()) {
            return;
        }
        LegacyMachineEffectPresenter.enqueueTexturedQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                RADAR_GUI_TEXTURE, LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                group -> {
                    for (RadarEntry entry : snapshot.entries()) {
                        LegacyRadarDisplayRenderer.emitWorldBlip(group, packedOverlay, entry,
                                snapshot.refPos(), snapshot.range());
                    }
                });
    }

    private static void enqueueNoiseOverlay(RadarScreenDisplayProfile.WorldOverlay overlay, PoseStack poseStack,
            MultiBufferSource buffer, int packedOverlay) {
        LegacyMachineEffectPresenter.enqueueTexturedQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                RADAR_GUI_TEXTURE, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                group -> LegacyRadarDisplayRenderer.emitWorldNoise(group, packedOverlay,
                        LegacyRadarDisplayRenderer.noiseV(overlay.noiseSeed())));
    }
}
