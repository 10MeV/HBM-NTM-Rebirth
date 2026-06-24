package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidDuctExhaustBlock;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FluidDuctItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final String[] PIPE_BASE_TEXTURES = {"pipe_neo", "pipe_silver", "pipe_colored"};
    private static final String[] PIPE_OVERLAY_TEXTURES = {"pipe_neo_overlay", "pipe_silver_overlay", "pipe_colored_overlay"};
    private static final String[] BOX_MATERIALS = {"silver", "copper", "white"};
    private static final String[] PIPE_INVENTORY_PARTS = {"pX", "nX", "pZ", "nZ"};
    private static final ResourceLocation[] PIPE_BASE_TEXTURE_LOCATIONS = buildPipeTextures(PIPE_BASE_TEXTURES);
    private static final ResourceLocation[] PIPE_OVERLAY_TEXTURE_LOCATIONS = buildPipeTextures(PIPE_OVERLAY_TEXTURES);
    private static final LegacyWavefrontModel.SelectionHandle PIPE_INVENTORY_HANDLE =
            ObjBlockModels.PIPE_NEO.prepareRenderOnlyInCallOrder(PIPE_INVENTORY_PARTS);

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
        int clampedStyle = Math.max(0, Math.min(PIPE_BASE_TEXTURES.length - 1, style));
        int color = HbmFluids.NONE.getColor();
        poseStack.pushPose();
        applyCenteredDisplay(displayContext, poseStack);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, Blocks.AIR.defaultBlockState(),
                packedLight, packedOverlay);
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(PIPE_BASE_TEXTURE_LOCATIONS[clampedStyle], context,
                PIPE_INVENTORY_HANDLE);
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(PIPE_OVERLAY_TEXTURE_LOCATIONS[clampedStyle],
                context.withColor(color), PIPE_INVENTORY_HANDLE);
        poseStack.popPose();
    }

    private static void renderBoxDuctItem(int metadata, boolean exhaust, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int clampedMetadata = FluidDuctBoxBlock.clampLegacyMetadata(metadata);
        String prefix = exhaust ? "boxduct_exhaust"
                : "boxduct_" + BOX_MATERIALS[FluidDuctBoxBlock.rectifyLegacyMaterial(clampedMetadata)];
        TextureAtlasSprite straight = sprite(prefix + "_straight");
        TextureAtlasSprite end = sprite(prefix + "_end");
        FluidDuctBoxBlock.DuctBounds bounds = FluidDuctBoxBlock.boundsFor(clampedMetadata);
        BlockState state = Blocks.AIR.defaultBlockState();
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);

        poseStack.pushPose();
        applyBlockDisplay(displayContext, poseStack);
        LegacyAtlasCuboidRenderer.croppedCuboid(straight, straight, end, end, straight, straight,
                context, bounds.lower(), bounds.lower(), 0.0D, bounds.upper(), bounds.upper(), 1.0D);
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

    private static ResourceLocation[] buildPipeTextures(String[] names) {
        ResourceLocation[] textures = new ResourceLocation[names.length];
        for (int i = 0; i < names.length; i++) {
            textures[i] = ObjBlockModels.texture(names[i]);
        }
        return textures;
    }
}
