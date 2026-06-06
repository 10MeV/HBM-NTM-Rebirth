package com.hbm.ntm.client.renderer;

import com.hbm.ntm.entity.item.MovingItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class MovingItemRenderer extends EntityRenderer<MovingItemEntity> {
    private final ItemRenderer itemRenderer;

    public MovingItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(MovingItemEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        Random random = new Random(entity.getId());
        poseStack.translate(0.0D, random.nextDouble() * 0.0625D, 0.0D);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingItemEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
