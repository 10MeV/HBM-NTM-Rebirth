package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.entity.RadarScreenDisplayProfile;
import com.hbm.ntm.api.entity.RadarScreenSnapshot;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
    public int getViewDistance() {
        return RadarScreenDisplayProfile.VIEW_DISTANCE;
    }

    @Override
    public void render(RadarScreenBlockEntity screen, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = screen.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(screen, state, definition, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        ObjModelLibrary.MACHINE_RADAR_SCREEN_LEGACY.renderAll(definition.textureLocation(),
                poseStack, buffer, modelLight, packedOverlay);

        ObjRenderContext overlayContext = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .fullBright()
                .withTranslucencyNoDepthWrite();
        RadarScreenSnapshot snapshot = screen.getSnapshot();
        Level level = screen.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        RadarScreenDisplayProfile.WorldOverlay overlay =
                RadarScreenDisplayProfile.overlay(snapshot, gameTime, partialTick, screen.getBlockPos());
        if (overlay.linked()) {
            renderLinkedOverlay(overlay, overlayContext);
        } else {
            renderNoiseOverlay(overlay, overlayContext);
        }

        poseStack.popPose();
    }

    private static void renderLinkedOverlay(RadarScreenDisplayProfile.WorldOverlay overlay, ObjRenderContext context) {
        LegacyRadarDisplayRenderer.renderWorldLinkedSweep(context, overlay.sweepOffset());

        RadarScreenDisplayProfile.forEachWorldBlip(overlay.snapshot(),
                blip -> LegacyRadarDisplayRenderer.renderWorldBlip(RADAR_GUI_TEXTURE, context,
                        blip.entry(), blip.reference(), blip.range()));
    }

    private static void renderNoiseOverlay(RadarScreenDisplayProfile.WorldOverlay overlay, ObjRenderContext context) {
        LegacyRadarDisplayRenderer.renderWorldNoise(RADAR_GUI_TEXTURE, context,
                LegacyRadarDisplayRenderer.noiseV(overlay.noiseSeed()));
    }
}
