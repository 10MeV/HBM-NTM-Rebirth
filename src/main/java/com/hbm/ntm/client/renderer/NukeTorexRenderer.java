package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyBillboardRenderer;
import com.hbm.ntm.client.obj.LegacyBillboardRenderer.CameraBasis;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.entity.effect.NukeTorexEntity.Cloudlet;
import com.hbm.ntm.entity.effect.NukeTorexEntity.TorexType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class NukeTorexRenderer extends EntityRenderer<NukeTorexEntity> {
    private static final ResourceLocation CLOUDLET_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/particle_base.png");
    private static final ResourceLocation FLARE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/flare.png");
    private static final RenderType CLOUDLET_RENDER_TYPE =
            LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE.renderType(CLOUDLET_TEXTURE);
    private static final RenderType FLARE_RENDER_TYPE =
            LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE.renderType(FLARE_TEXTURE);
    private static final Comparator<RenderCloudlet> GLOBAL_FAR_TO_NEAR =
            Comparator.comparingDouble((RenderCloudlet entry) -> entry.distanceToCameraSqr).reversed();

    public NukeTorexRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(NukeTorexEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        CameraBasis cameraBasis = LegacyBillboardRenderer.cameraBasis(camera);

        poseStack.pushPose();
        int visualAge = Math.max(entity.tickCount, entity.getSyncedAge());
        if (visualAge < 101) {
            renderFlare(entity, partialTick, poseStack, buffer, cameraBasis);
        }
        applyPlayerShake(entity);
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    public static void renderCloudletsAfterLevel(ClientLevel level, Camera camera, float partialTick,
            PoseStack poseStack, MultiBufferSource.BufferSource buffer) {
        List<RenderCloudlet> cloudlets = collectCloudlets(level, camera.getPosition());
        if (cloudlets.isEmpty()) {
            return;
        }

        cloudlets.sort(GLOBAL_FAR_TO_NEAR);
        PoseStack.Pose pose = poseStack.last();
        Vec3 cameraPos = camera.getPosition();
        CameraBasis cameraBasis = LegacyBillboardRenderer.cameraBasis(camera);

        VertexConsumer consumer = buffer.getBuffer(CLOUDLET_RENDER_TYPE);
        for (RenderCloudlet entry : cloudlets) {
            renderBillboard(consumer, pose, cameraBasis, entry.cloudlet(), partialTick,
                    cameraPos.x, cameraPos.y, cameraPos.z);
        }
        buffer.endBatch(CLOUDLET_RENDER_TYPE);
    }

    private static List<RenderCloudlet> collectCloudlets(ClientLevel level, Vec3 cameraPos) {
        List<RenderCloudlet> entries = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof NukeTorexEntity torex) || torex.cloudlets.isEmpty()) {
                continue;
            }
            for (Cloudlet cloudlet : torex.cloudlets) {
                entries.add(new RenderCloudlet(cloudlet,
                        cameraPos.distanceToSqr(cloudlet.posX, cloudlet.posY, cloudlet.posZ)));
            }
        }
        return entries;
    }

    private static void renderBillboard(VertexConsumer consumer, PoseStack.Pose pose, CameraBasis cameraBasis, Cloudlet cloudlet,
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

        float brightness = cloudlet.type == TorexType.CONDENSATION ? 0.9F : 0.75F * cloudlet.colorMod;
        Vec3 color = cloudlet.getInterpColor(partialTick);
        float red = Mth.clamp((float) color.x * brightness, 0.0F, 1.0F);
        float green = Mth.clamp((float) color.y * brightness, 0.0F, 1.0F);
        float blue = Mth.clamp((float) color.z * brightness, 0.0F, 1.0F);
        int light = LightTexture.FULL_BRIGHT;

        LegacyBillboardRenderer.billboardRgbaF(consumer, pose, cameraBasis,
                x, y, z, scale, scale, red, green, blue, alpha, light);
    }

    private void renderFlare(NukeTorexEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            CameraBasis cameraBasis) {
        double age = Math.min(entity.tickCount + partialTick, 100.0D);
        float alpha = (float) ((100.0D - age) / 100.0D);
        Random random = new Random(entity.getId());
        VertexConsumer consumer = buffer.getBuffer(FLARE_RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();

        for (int i = 0; i < 3; i++) {
            float x = (float) (random.nextGaussian() * 0.5F * entity.rollerSize);
            float y = (float) (random.nextGaussian() * 0.5F * entity.rollerSize + entity.coreHeight);
            float z = (float) (random.nextGaussian() * 0.5F * entity.rollerSize);
            float scale = (float) (25.0D * entity.rollerSize);

            LegacyBillboardRenderer.billboardRgbaF(consumer, pose, cameraBasis,
                    x, y, z, scale, scale, 1.0F, 1.0F, 1.0F, alpha, LightTexture.FULL_BRIGHT);
        }
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

    private record RenderCloudlet(Cloudlet cloudlet, double distanceToCameraSqr) {
    }

    @Override
    public ResourceLocation getTextureLocation(NukeTorexEntity entity) {
        return CLOUDLET_TEXTURE;
    }
}
