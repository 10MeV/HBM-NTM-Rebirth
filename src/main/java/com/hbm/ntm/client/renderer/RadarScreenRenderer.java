package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RadarScreenRenderer implements BlockEntityRenderer<RadarScreenBlockEntity> {
    private static final ResourceLocation RADAR_GUI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_nt.png");
    private static final double TEXTURE_SIZE = 256.0D;

    public RadarScreenRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(RadarScreenBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
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

        ObjModelLibrary.MACHINE_RADAR_SCREEN_LEGACY.renderAll(definition.textureLocation(),
                poseStack, buffer, modelLight, packedOverlay);

        ObjRenderContext overlayContext = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .fullBright()
                .withTranslucencyNoDepthWrite();
        if (screen.isLinked()) {
            renderLinkedOverlay(screen, partialTick, overlayContext);
        } else {
            renderNoiseOverlay(screen, overlayContext);
        }

        poseStack.popPose();
    }

    private static void renderLinkedOverlay(RadarScreenBlockEntity screen, float partialTick,
            ObjRenderContext context) {
        Level level = screen.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        double offset = ((gameTime % 56L) + partialTick) / 30.0D;
        LegacyUntexturedQuadRenderer.doubleSidedQuad(context,
                0.38D, 2.0D - offset, 1.375D,
                0.38D, 2.0D - offset, -0.375D,
                0.38D, 2.0D - offset - 0.125D, -0.375D,
                0.38D, 2.0D - offset - 0.125D, 1.375D,
                0x00FF00, 0, 0, 50, 50);

        BlockPos ref = screen.getRefPos();
        double divisor = (double) screen.getRange() + 1.0D;
        for (RadarEntry entry : screen.getEntries()) {
            double sX = (entry.pos().getX() - ref.getX()) / divisor * 0.875D;
            double sZ = (entry.pos().getZ() - ref.getZ()) / divisor * 0.875D;
            double size = 0.0625D;
            double v0 = entry.blipLevel() * 8.0D;
            double v1 = v0 + 8.0D;
            LegacyTexturedQuadRenderer.pixelQuad(RADAR_GUI_TEXTURE, context,
                    0.0F, 1.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE,
                    0.38D, 1.0D - sZ + size, 0.5D - sX + size, 216.0D, v1,
                    0.38D, 1.0D - sZ + size, 0.5D - sX - size, 224.0D, v1,
                    0.38D, 1.0D - sZ - size, 0.5D - sX - size, 224.0D, v0,
                    0.38D, 1.0D - sZ - size, 0.5D - sX + size, 216.0D, v0,
                    0xFFFFFF, 255);
        }
    }

    private static void renderNoiseOverlay(RadarScreenBlockEntity screen, ObjRenderContext context) {
        Level level = screen.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        int offset = 118 + (int) Math.floorMod(gameTime * 31L + screen.getBlockPos().asLong(), 81L);
        LegacyTexturedQuadRenderer.pixelQuad(RADAR_GUI_TEXTURE, context.withoutTranslucency(),
                0.0F, 1.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE,
                0.38D, 1.875D, 1.375D, 216.0D, offset + 40.0D,
                0.38D, 1.875D, -0.375D, 256.0D, offset + 40.0D,
                0.38D, 0.125D, -0.375D, 256.0D, offset,
                0.38D, 0.125D, 1.375D, 216.0D, offset,
                0xFFFFFF, 255);
    }
}
