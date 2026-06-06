package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.item.TrinketBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class TrinketItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final TrinketItemRenderer INSTANCE = new TrinketItemRenderer(
            net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            net.minecraft.client.Minecraft.getInstance().getEntityModels());

    private TrinketItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof TrinketBlockItem item)) {
            return;
        }

        int variant = TrinketVariant.clamp(item.kind(), TrinketBlockItem.getVariant(stack));
        if (variant <= 0) {
            return;
        }

        poseStack.pushPose();
        applyItemDisplay(item.kind(), variant, displayContext, poseStack);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, Blocks.AIR.defaultBlockState(), packedLight, packedOverlay);
        TrinketBlockEntityRenderer.renderTrinket(item.kind(), variant, 0, 0.0F, context);
        poseStack.popPose();
    }

    private static void applyItemDisplay(TrinketVariant.Kind kind, int variant, ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            applyGuiDisplay(kind, variant, poseStack);
            return;
        }

        poseStack.translate(0.5D, 0.0D, 0.5D);
        switch (kind) {
            case BOBBLEHEAD -> {
                poseStack.translate(0.0D, -0.35D, 0.0D);
                poseStack.scale(0.9F, 0.9F, 0.9F);
            }
            case SNOWGLOBE -> {
                poseStack.translate(0.0D, -0.12D, 0.0D);
                poseStack.scale(1.45F, 1.45F, 1.45F);
            }
            case PLUSHIE -> {
                poseStack.translate(0.0D, -0.2D, 0.0D);
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }
        }

        if (displayContext.firstPerson()) {
            poseStack.scale(0.75F, 0.75F, 0.75F);
        } else if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.55F, 0.55F, 0.55F);
        }
    }

    private static void applyGuiDisplay(TrinketVariant.Kind kind, int variant, PoseStack poseStack) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.scale(-1.0F, 1.0F, -1.0F);

        switch (kind) {
            case BOBBLEHEAD -> {
                poseStack.scale(1.25F, 1.25F, 1.25F);
                poseStack.translate(-0.02675D, -0.36313D, 0.0D);
            }
            case SNOWGLOBE -> {
                poseStack.scale(1.35F, 1.35F, 1.35F);
                poseStack.translate(0.0D, -0.15625D, 0.0D);
            }
            case PLUSHIE -> applyPlushieGuiDisplay(variant, poseStack);
        }
    }

    private static void applyPlushieGuiDisplay(int variant, PoseStack poseStack) {
        String suffix = TrinketVariant.modelSuffix(TrinketVariant.Kind.PLUSHIE, variant);
        switch (suffix) {
            case "yomi" -> {
                poseStack.scale(0.95F, 0.95F, 0.95F);
                poseStack.translate(-0.07825D, -0.45313D, 0.0D);
            }
            case "numbernine" -> {
                poseStack.scale(0.65F, 0.65F, 0.65F);
                poseStack.translate(-0.11719D, -0.67969D, 0.0D);
            }
            case "hundun" -> {
                poseStack.scale(0.22F, 0.22F, 0.22F);
                poseStack.translate(0.84375D, -0.92969D, -1.08398D);
            }
            case "derg" -> {
                poseStack.scale(0.68F, 0.68F, 0.68F);
                poseStack.translate(0.00195D, -0.64453D, 0.0D);
            }
            default -> {
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }
        }
    }
}
