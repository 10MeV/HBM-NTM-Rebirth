package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidDuctExhaustBlock;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FluidDuctVariantBlockItem;
import com.hbm.ntm.item.FluidPipeStyleBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FluidDuctItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final String[] PIPE_INVENTORY_PARTS = {"pX", "nX", "pZ", "nZ"};
    private static final ResourceLocation[] PIPE_BASE_TEXTURE_LOCATIONS = buildPipeTextures(false);
    private static final ResourceLocation[] PIPE_OVERLAY_TEXTURE_LOCATIONS = buildPipeTextures(true);
    private static final LegacyWavefrontModel.SelectionHandle PIPE_INVENTORY_HANDLE =
            ObjBlockModels.PIPE_NEO.prepareRenderOnlyInCallOrder(PIPE_INVENTORY_PARTS);
    private static final BoxDuctItemTextures[] BOX_ITEM_TEXTURES_BY_METADATA = buildBoxItemTextures(false);
    private static final BoxDuctItemTextures[] EXHAUST_ITEM_TEXTURES_BY_METADATA = buildBoxItemTextures(true);

    public static final FluidDuctItemRenderer INSTANCE = new FluidDuctItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private FluidDuctItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (stack.getItem() instanceof FluidPipeStyleBlockItem pipeItem) {
            renderPipeItem(pipeItem.getVariant(stack), displayContext, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
        if (stack.getItem() instanceof FluidDuctVariantBlockItem ductItem) {
            boolean exhaust = stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof FluidDuctExhaustBlock;
            renderBoxDuctItem(ductItem.getLegacyMetadata(stack), exhaust, displayContext, poseStack, buffer,
                    packedLight, packedOverlay);
        }
    }

    private static void renderPipeItem(int style, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int clampedStyle = HbmFluidDuctVariants.clampStandardStyle(style);
        int color = HbmFluids.NONE.getColor();
        poseStack.pushPose();
        applyCenteredDisplay(displayContext, poseStack);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(PIPE_BASE_TEXTURE_LOCATIONS[clampedStyle], poseStack, buffer,
                packedLight, packedOverlay, PIPE_INVENTORY_HANDLE);
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(PIPE_OVERLAY_TEXTURE_LOCATIONS[clampedStyle],
                poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255, false, PIPE_INVENTORY_HANDLE);
        poseStack.popPose();
    }

    private static void renderBoxDuctItem(int metadata, boolean exhaust, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int clampedMetadata = FluidDuctBoxBlock.clampLegacyMetadata(metadata);
        BoxDuctItemTextures textures = exhaust ? EXHAUST_ITEM_TEXTURES_BY_METADATA[clampedMetadata]
                : BOX_ITEM_TEXTURES_BY_METADATA[clampedMetadata];
        FluidDuctBoxBlock.DuctBounds bounds = FluidDuctBoxBlock.boundsFor(clampedMetadata);

        poseStack.pushPose();
        applyBlockDisplay(displayContext, poseStack);
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.straight(), textures.straight(), textures.end(),
                textures.end(), textures.straight(), textures.straight(),
                poseStack, buffer, packedLight, packedOverlay, bounds.lower(), bounds.lower(), 0.0D,
                bounds.upper(), bounds.upper(), 1.0D);
        poseStack.popPose();
    }

    private static void applyCenteredDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
            return;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.8F, 0.8F, 0.8F);
        } else if (displayContext.firstPerson()) {
            poseStack.scale(0.9F, 0.9F, 0.9F);
        }
    }

    private static void applyBlockDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        applyCenteredDisplay(displayContext, poseStack);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }

    private static TextureAtlasSprite sprite(String texture) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + texture));
    }

    private static ResourceLocation[] buildPipeTextures(boolean overlay) {
        ResourceLocation[] textures = new ResourceLocation[HbmFluidDuctVariants.STANDARD_STYLE_COUNT];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = ObjBlockModels.texture(overlay
                    ? HbmFluidDuctVariants.standardOverlayTexture(i)
                    : HbmFluidDuctVariants.standardParticleTexture(i));
        }
        return textures;
    }

    private static BoxDuctItemTextures[] buildBoxItemTextures(boolean exhaust) {
        BoxDuctItemTextures[] textures = new BoxDuctItemTextures[HbmFluidDuctVariants.BOX_METADATA_COUNT];
        for (int metadata = 0; metadata < textures.length; metadata++) {
            String prefix = exhaust ? "boxduct_exhaust"
                    : "boxduct_" + HbmFluidDuctVariants.boxMaterialTexture(metadata);
            textures[metadata] = new BoxDuctItemTextures(sprite(prefix + "_straight"), sprite(prefix + "_end"));
        }
        return textures;
    }

    private record BoxDuctItemTextures(TextureAtlasSprite straight, TextureAtlasSprite end) {
    }
}
