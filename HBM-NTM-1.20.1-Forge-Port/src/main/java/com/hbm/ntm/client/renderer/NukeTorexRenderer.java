package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.entity.effect.NukeTorexEntity.Cloudlet;
import com.hbm.ntm.entity.effect.NukeTorexEntity.TorexType;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.Random;

public class NukeTorexRenderer extends EntityRenderer<NukeTorexEntity> {
    private static final ResourceLocation CLOUDLET_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/particle_base.png");
    private static final ResourceLocation FLARE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/flare.png");
    private static final RenderType CLOUDLET_RENDER_TYPE = translucentDepthWrite("hbm_torex_cloudlet",
            CLOUDLET_TEXTURE);
    private static final RenderType FLARE_RENDER_TYPE = translucentNoDepth("hbm_torex_flare", FLARE_TEXTURE, true);
    private static final Comparator<Cloudlet> FAR_TO_NEAR =
            (first, second) -> Double.compare(second.renderSortDistanceSq, first.renderSortDistanceSq);

    private final Vector3f cameraRight = new Vector3f();
    private final Vector3f cameraUp = new Vector3f();

    public NukeTorexRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(NukeTorexEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Quaternionf rotation = camera.rotation();
        cameraRight.set(1.0F, 0.0F, 0.0F).rotate(rotation);
        cameraUp.set(0.0F, 1.0F, 0.0F).rotate(rotation);

        poseStack.pushPose();
        int visualAge = Math.max(entity.tickCount, entity.getSyncedAge());
        if (visualAge < 101) {
            renderFlare(entity, partialTick, poseStack, buffer);
        }
        applyPlayerShake(entity);
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    public void renderCloudletsAfterLevel(NukeTorexEntity entity, Camera camera, float partialTick,
            PoseStack poseStack, MultiBufferSource.BufferSource buffer) {
        if (entity.cloudlets.isEmpty()) {
            return;
        }

        Quaternionf rotation = camera.rotation();
        cameraRight.set(1.0F, 0.0F, 0.0F).rotate(rotation);
        cameraUp.set(0.0F, 1.0F, 0.0F).rotate(rotation);

        Vec3 cameraPos = camera.getPosition();
        double originX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double originY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double originZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        poseStack.pushPose();
        poseStack.translate(originX - cameraPos.x, originY - cameraPos.y, originZ - cameraPos.z);
        renderCloudlets(entity, cameraPos, partialTick, poseStack, buffer);
        poseStack.popPose();
        buffer.endBatch(CLOUDLET_RENDER_TYPE);
    }

    private void renderCloudlets(NukeTorexEntity entity, Vec3 cameraPos, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer) {
        sortCloudlets(entity, cameraPos);
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        double originX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double originY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double originZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        VertexConsumer consumer = buffer.getBuffer(CLOUDLET_RENDER_TYPE);
        for (Cloudlet cloudlet : entity.cloudlets) {
            renderBillboard(consumer, pose, normal, cloudlet, partialTick, originX, originY, originZ);
        }
    }

    private void renderBillboard(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, Cloudlet cloudlet,
            float partialTick, double originX, double originY, double originZ) {
        float alpha = cloudlet.getAlpha();
        if (alpha <= 0.001F) {
            return;
        }

        Vec3 pos = cloudlet.getInterpPos(partialTick);
        float x = (float) (pos.x - originX);
        float y = (float) (pos.y - originY);
        float z = (float) (pos.z - originZ);
        float scale = cloudlet.getScale();

        float rightX = cameraRight.x() * scale;
        float rightY = cameraRight.y() * scale;
        float rightZ = cameraRight.z() * scale;
        float upX = cameraUp.x() * scale;
        float upY = cameraUp.y() * scale;
        float upZ = cameraUp.z() * scale;

        float brightness = cloudlet.type == TorexType.CONDENSATION ? 0.9F : 0.75F * cloudlet.colorMod;
        Vec3 color = cloudlet.getInterpColor(partialTick);
        float red = Mth.clamp((float) color.x * brightness, 0.0F, 1.0F);
        float green = Mth.clamp((float) color.y * brightness, 0.0F, 1.0F);
        float blue = Mth.clamp((float) color.z * brightness, 0.0F, 1.0F);
        int light = LightTexture.FULL_BRIGHT;

        putVertex(consumer, pose, normal, x - rightX - upX, y - rightY - upY, z - rightZ - upZ,
                1.0F, 1.0F, red, green, blue, alpha, light);
        putVertex(consumer, pose, normal, x - rightX + upX, y - rightY + upY, z - rightZ + upZ,
                1.0F, 0.0F, red, green, blue, alpha, light);
        putVertex(consumer, pose, normal, x + rightX + upX, y + rightY + upY, z + rightZ + upZ,
                0.0F, 0.0F, red, green, blue, alpha, light);
        putVertex(consumer, pose, normal, x + rightX - upX, y + rightY - upY, z + rightZ - upZ,
                0.0F, 1.0F, red, green, blue, alpha, light);
    }

    private void renderFlare(NukeTorexEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        double age = Math.min(entity.tickCount + partialTick, 100.0D);
        float alpha = (float) ((100.0D - age) / 100.0D);
        Random random = new Random(entity.getId());
        VertexConsumer consumer = buffer.getBuffer(FLARE_RENDER_TYPE);
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (int i = 0; i < 3; i++) {
            float x = (float) (random.nextGaussian() * 0.5F * entity.rollerSize);
            float y = (float) (random.nextGaussian() * 0.5F * entity.rollerSize + entity.coreHeight);
            float z = (float) (random.nextGaussian() * 0.5F * entity.rollerSize);
            float scale = (float) (25.0D * entity.rollerSize);

            float rightX = cameraRight.x() * scale;
            float rightY = cameraRight.y() * scale;
            float rightZ = cameraRight.z() * scale;
            float upX = cameraUp.x() * scale;
            float upY = cameraUp.y() * scale;
            float upZ = cameraUp.z() * scale;

            putVertex(consumer, pose, normal, x - rightX - upX, y - rightY - upY, z - rightZ - upZ,
                    1.0F, 1.0F, 1.0F, 1.0F, 1.0F, alpha, LightTexture.FULL_BRIGHT);
            putVertex(consumer, pose, normal, x - rightX + upX, y - rightY + upY, z - rightZ + upZ,
                    1.0F, 0.0F, 1.0F, 1.0F, 1.0F, alpha, LightTexture.FULL_BRIGHT);
            putVertex(consumer, pose, normal, x + rightX + upX, y + rightY + upY, z + rightZ + upZ,
                    0.0F, 0.0F, 1.0F, 1.0F, 1.0F, alpha, LightTexture.FULL_BRIGHT);
            putVertex(consumer, pose, normal, x + rightX - upX, y + rightY - upY, z + rightZ - upZ,
                    0.0F, 1.0F, 1.0F, 1.0F, 1.0F, alpha, LightTexture.FULL_BRIGHT);
        }
    }

    private void sortCloudlets(NukeTorexEntity entity, Vec3 cameraPos) {
        int clientTick = Minecraft.getInstance().gui.getGuiTicks();
        if (entity.lastRenderSortTick == clientTick) {
            return;
        }

        for (Cloudlet cloudlet : entity.cloudlets) {
            cloudlet.renderSortDistanceSq = cameraPos.distanceToSqr(cloudlet.posX, cloudlet.posY, cloudlet.posZ);
        }
        entity.cloudlets.sort(FAR_TO_NEAR);
        entity.lastRenderSortTick = clientTick;
    }

    private void applyPlayerShake(NukeTorexEntity entity) {
        if (!entity.didPlaySound || entity.didShake) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        entity.applyClientShockwaveShake(player);
    }

    private static RenderType translucentNoDepth(String name, ResourceLocation texture, boolean additive) {
        RenderStateShard.TransparencyStateShard transparency = new RenderStateShard.TransparencyStateShard(
                name + "_transparency",
                () -> {
                    RenderSystem.enableBlend();
                    if (additive) {
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    } else {
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    }
                },
                () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                });

        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                false, true, RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setCullState(new RenderStateShard.CullStateShard(false))
                        .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                        .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                        .createCompositeState(false));
    }

    private static RenderType translucentDepthWrite(String name, ResourceLocation texture) {
        RenderStateShard.TransparencyStateShard transparency = new RenderStateShard.TransparencyStateShard(
                name + "_transparency",
                () -> {
                    RenderSystem.depthMask(true);
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                            GlStateManager.SourceFactor.ONE,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                },
                () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                });

        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                false, true, RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setCullState(new RenderStateShard.CullStateShard(false))
                        .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                        .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                        .createCompositeState(false));
    }

    private static void putVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float z,
            float u, float v, float red, float green, float blue, float alpha, int light) {
        consumer.vertex(pose, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(NukeTorexEntity entity) {
        return CLOUDLET_TEXTURE;
    }
}
