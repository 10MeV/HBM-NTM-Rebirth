package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BalefireBombBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.BalefireBombBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjGlintRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjBombModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BalefireBombRenderer implements BlockEntityRenderer<BalefireBombBlockEntity> {
    public BalefireBombRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BalefireBombBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof BalefireBombBlock)) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(state)));
        renderModel(poseStack, buffer, modelLight, packedOverlay);

        if (blockEntity.isLoadedSynced()) {
            float age = glintAge(blockEntity.getLevel(), partialTick);
            LegacyObjGlintRenderer.renderClassicGlint(ObjBombModels.FSTBMB,
                    LegacyObjGlintRenderer.BALEFIRE_GLINT_TEXTURE, poseStack, buffer, modelLight, packedOverlay,
                    ObjBombModels.FSTBMB_BALEFIRE, age, 0.0F, 0.8F, 0.15F, 5.0F, 2.0F);
            renderTimer(blockEntity, poseStack, buffer);
        }

        poseStack.popPose();
    }

    public static void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjBombModels.renderFstbmbBody(poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public static void applyLegacyItemCommon(PoseStack poseStack) {
        poseStack.translate(1.0D, 0.0D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    }

    private static void renderTimer(BalefireBombBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer) {
        Font font = Minecraft.getInstance().font;
        String text = blockEntity.getMinutesText() + ":" + blockEntity.getSecondsText();

        poseStack.pushPose();
        poseStack.translate(0.815F, 0.9275F, 0.5F);
        poseStack.scale(0.04F, -0.04F, 0.04F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, 1.0F, 0.0F);
        font.drawInBatch(text, 0.0F, 0.0F, 0xFF0000, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static float legacyYaw(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return switch (facing) {
            case NORTH -> 90.0F;
            case EAST -> 0.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            default -> 270.0F;
        };
    }

    private static float glintAge(Level level, float partialTick) {
        return level == null ? partialTick : level.getGameTime() + partialTick;
    }

    @Override
    public boolean shouldRenderOffScreen(BalefireBombBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }
}
