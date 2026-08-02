package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.entity.logic.OrbitalLaserEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Exact untextured two-pass geometry of 1.7.10 {@code RenderOrbitalLaser}. */
public final class OrbitalLaserRenderer extends EntityRenderer<OrbitalLaserEntity> {
    private static final double BEAM_HEIGHT = 250.0D;
    private static final int BEAM_SEGMENTS = 8;
    // Vec3#rotateAroundY consumed radians in 1.7.10; the source passed 45.
    private static final float LEGACY_ROTATION_RADIANS = 45.0F;

    public OrbitalLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(OrbitalLaserEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 180));
        PoseStack.Pose pose = poseStack.last();
        renderBeam(consumer, pose, 0.5F, 0.0F, 255, 0, 0);
        // The old renderer retains the Vec3 after its red loop.
        renderBeam(consumer, pose, 0.25F, BEAM_SEGMENTS * LEGACY_ROTATION_RADIANS, 255, 255, 255);
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderBeam(VertexConsumer consumer, PoseStack.Pose pose, float radius, float startAngle,
            int red, int green, int blue) {
        int color = red << 16 | green << 8 | blue;
        for (int i = 0; i < BEAM_SEGMENTS; i++) {
            float firstAngle = startAngle + i * LEGACY_ROTATION_RADIANS;
            float secondAngle = firstAngle + LEGACY_ROTATION_RADIANS;
            float x1 = Mth.cos(firstAngle) * radius;
            float z1 = -Mth.sin(firstAngle) * radius;
            float x2 = Mth.cos(secondAngle) * radius;
            float z2 = -Mth.sin(secondAngle) * radius;
            LegacyUntexturedQuadRenderer.quad(consumer, pose,
                    x1, BEAM_HEIGHT, z1,
                    x1, 0.0D, z1,
                    x2, 0.0D, z2,
                    x2, BEAM_HEIGHT, z2,
                    color, 255, 255, 255, 255);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(OrbitalLaserEntity entity) {
        return null;
    }
}
