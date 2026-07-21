package com.hbm.ntm.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Explicit no-op renderer for source-invisible rail-car collision and seat dummies. */
public final class InvisibleEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");

    public InvisibleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        // Intentionally invisible: the source entities were only collision/rider proxies.
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return EMPTY_TEXTURE;
    }
}
