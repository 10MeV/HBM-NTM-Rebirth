package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.OreSlopperBlockEntity;
import com.hbm.ntm.blockentity.OreSlopperBlockEntity.SlopperAnimation;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.item.BedrockOreItem;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreGrade;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OreSlopperRenderer implements BlockEntityRenderer<OreSlopperBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_ORE_SLOPPER;

    public OreSlopperRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(OreSlopperBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(OreSlopperBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay)
                .withRenderMode(LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
        MODEL.renderPart("Base", definition.textureLocation(), context);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, blockEntity.getSlider(partialTick) * -3.0D);
        MODEL.renderPart("Slider", definition.textureLocation(), context);

        poseStack.pushPose();
        double extend = blockEntity.getBucket(partialTick) * 1.5D;
        poseStack.translate(0.0D, -Mth.clamp(extend - 0.25D, 0.0D, 1.25D), 0.0D);
        MODEL.renderPart("Hydraulics", definition.textureLocation(), context);
        poseStack.translate(0.0D, -Mth.clamp(extend, 0.0D, 1.25D), 0.0D);
        MODEL.renderPart("Bucket", definition.textureLocation(), context);
        if (blockEntity.getAnimation() == SlopperAnimation.LIFTING) {
            renderBucketOre(blockEntity, poseStack, buffer, packedLight);
        }
        poseStack.popPose();
        poseStack.popPose();

        double blades = blockEntity.getBlades(partialTick);
        poseStack.pushPose();
        poseStack.translate(0.375D, 2.75D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) blades));
        poseStack.translate(-0.375D, -2.75D, 0.0D);
        MODEL.renderPart("BladesLeft", definition.textureLocation(), context);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.375D, 2.75D, 0.0D);
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) blades));
        poseStack.translate(0.375D, -2.75D, 0.0D);
        MODEL.renderPart("BladesRight", definition.textureLocation(), context);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.875D, -1.0D);
        poseStack.mulPose(Axis.XN.rotationDegrees((float) blockEntity.getFan(partialTick)));
        poseStack.translate(0.0D, -1.875D, 1.0D);
        MODEL.renderPart("Fan", definition.textureLocation(), context);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderBucketOre(OreSlopperBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ItemStack stack = BedrockOreItem.make(BedrockOreGrade.BASE, BedrockOreType.LIGHT_METAL);
        poseStack.pushPose();
        poseStack.translate(0.0625D, 4.3125D, 2.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(1.75F, 1.75F, 1.75F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0);
        poseStack.popPose();
    }
}
