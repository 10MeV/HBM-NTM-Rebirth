package com.hbm.ntm.client.renderer;

import com.hbm.ntm.entity.mob.EntityUndeadSoldier;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Native-model counterpart of 1.7.10 RenderUndeadSoldier. */
public final class UndeadSoldierRenderer extends MobRenderer<EntityUndeadSoldier, HumanoidModel<EntityUndeadSoldier>> {
    private static final ResourceLocation ZOMBIE = new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");
    private static final ResourceLocation SKELETON = new ResourceLocation("minecraft", "textures/entity/skeleton/skeleton.png");
    private final HumanoidModel<EntityUndeadSoldier> zombie;
    private final HumanoidModel<EntityUndeadSoldier> skeleton;

    public UndeadSoldierRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        zombie = model;
        // ModelSkeletonNT inherited ModelZombie animation and changed only limb geometry.
        skeleton = new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON));
    }

    @Override
    public void render(EntityUndeadSoldier entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int light) {
        model = entity.isLegacySkeleton() ? skeleton : zombie;
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityUndeadSoldier entity) {
        return entity.isLegacySkeleton() ? SKELETON : ZOMBIE;
    }
}
