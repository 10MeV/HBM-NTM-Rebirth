package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctPaintableExhaustBlock;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.blockentity.PaintableDuctBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidDuctPaintableRenderer<T extends BlockEntity & PaintableDuctBlockEntity>
        implements BlockEntityRenderer<T> {
    private static final ResourceLocation DUCT_BASE =
            new ResourceLocation(HbmNtm.MOD_ID, "block/fluid_duct_paintable");
    private static final ResourceLocation DUCT_EXHAUST_BASE =
            new ResourceLocation(HbmNtm.MOD_ID, "block/fluid_duct_paintable_block_exhaust");
    private static final ResourceLocation DUCT_OVERLAY =
            new ResourceLocation(HbmNtm.MOD_ID, "block/fluid_duct_paintable_overlay");
    private static final ResourceLocation DUCT_COLOR_OVERLAY =
            new ResourceLocation(HbmNtm.MOD_ID, "block/fluid_duct_paintable_color");

    public FluidDuctPaintableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(T duct, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        BlockState state = duct.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(duct, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
        BlockState painted = duct.getPaintedState();
        boolean exhaust = state.getBlock() instanceof FluidDuctPaintableExhaustBlock;
        boolean overlay = state.hasProperty(com.hbm.ntm.block.FluidDuctPaintableBlock.OVERLAY)
                && state.getValue(com.hbm.ntm.block.FluidDuctPaintableBlock.OVERLAY);

        poseStack.pushPose();
        if (painted != null) {
            HbmClientRenderUtil.renderSingleBlock(Minecraft.getInstance().getBlockRenderer(), painted, poseStack,
                    buffer, modelLight);
            if (overlay) {
                renderCube(sprite(DUCT_OVERLAY), context);
            }
        } else {
            renderCube(sprite(exhaust ? DUCT_EXHAUST_BASE : DUCT_BASE), context);
            if (!exhaust) {
                int color = duct instanceof FluidPipeBlockEntity pipe ? pipe.getFluidType().getColor() : 0xFFFFFF;
                renderCube(sprite(DUCT_COLOR_OVERLAY), context.withColor(color));
            }
        }
        poseStack.popPose();
    }

    private static void renderCube(TextureAtlasSprite sprite, ObjRenderContext context) {
        LegacyAtlasCuboidRenderer.croppedCuboid(sprite, context, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    private static TextureAtlasSprite sprite(ResourceLocation texture) {
        return LegacyTexturedQuadRenderer.blockSprite(texture);
    }
}
