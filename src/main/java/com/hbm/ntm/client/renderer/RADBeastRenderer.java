package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.entity.mob.EntityRADBeast;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class RADBeastRenderer extends MobRenderer<EntityRADBeast, BlazeModel<EntityRADBeast>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/entity/radbeast.png");
    private static final ResourceLocation MASK_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/model_m65_blaze.png");
    private static final int BEAM_COLOR = 0x004000;

    public RADBeastRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel<>(context.bakeLayer(ModelLayers.BLAZE)), 0.5F);
        addLayer(new M65BlazeLayer(this));
    }

    @Override
    public void render(EntityRADBeast entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        renderBeam(entity, poseStack, buffer);
        super.render(entity, yaw, partialTick, poseStack, buffer, LightTexture.FULL_BRIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRADBeast entity) {
        return TEXTURE;
    }

    private static void renderBeam(EntityRADBeast entity, PoseStack poseStack, MultiBufferSource buffer) {
        Entity victim = entity.getUnfortunateSoul();
        Level level = entity.level();
        // RenderRADBeast used Y > 0.1 solely as the 1.7.10 bottom-of-world guard.
        // Negative modern terrain must not suppress the beam above its real floor.
        if (victim == null || entity.getY() <= level.getMinBuildHeight() + 0.1D) {
            return;
        }

        double sourceY = entity.getY() + 1.25D;
        double targetY = victim.getY() + victim.getBbHeight() / 2.0D;
        if (victim == Minecraft.getInstance().player) {
            targetY -= 1.5D;
        }

        double dx = victim.getX() - entity.getX();
        double dy = targetY - sourceY;
        double dz = victim.getZ() - entity.getZ();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length >= 200.0D) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.25D, 0.0D);
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        LegacyBeamRenderer.solidBeam(beamBatch, dx, dy, dz, LegacyBeamRenderer.WaveType.RANDOM,
                BEAM_COLOR, BEAM_COLOR, (int) (level.getGameTime() % 1000L) + 1,
                (int) (length * 5.0D), 0.125F, 2, 0.03125F);
        poseStack.popPose();
    }

    private static final class M65BlazeLayer extends RenderLayer<EntityRADBeast, BlazeModel<EntityRADBeast>> {
        private static final ModelPart M65_ROOT = m65BlazeLayer().bakeRoot();

        private M65BlazeLayer(RenderLayerParent<EntityRADBeast, BlazeModel<EntityRADBeast>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EntityRADBeast entity,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                float headPitch) {
            poseStack.pushPose();
            getParentModel().root().getChild("head").translateAndRotate(poseStack);
            float scale = 18.0F / 16.0F;
            poseStack.scale(scale, scale, scale);
            poseStack.scale(1.01F, 1.01F, 1.01F);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(MASK_TEXTURE));
            M65_ROOT.getChild("mask").render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            M65_ROOT.getChild("filter").render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        private static LayerDefinition m65BlazeLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            PartDefinition mask = root.addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.ZERO);
            PartDefinition filter = root.addOrReplaceChild("filter", CubeListBuilder.create(), PartPose.ZERO);
            float y = 4.0F;
            cube(mask, "head", 0, 0, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F,
                    -4.0F, -8.0F + y, -4.0F, 0.0F, 0.0F, 0.0F);
            cube(mask, "nose", 0, 16, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 1.0F,
                    -1.5F, -3.5F + y, -5.0F, 0.0F, 0.0F, 0.0F);
            cube(mask, "outlet", 0, 20, 0.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F,
                    -1.0F, -3.5F + y, -5.0F, -0.4799655F, 0.0F, 0.0F);
            cube(mask, "nose_slope", 8, 16, 0.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F,
                    -1.5F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
            cube(mask, "left_eye", 0, 23, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 0.0F,
                    -3.5F, -6.0F + y, -4.2F, 0.0F, 0.0F, 0.0F);
            cube(mask, "right_eye", 0, 26, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 0.0F,
                    0.5F, -6.0F + y, -4.2F, 0.0F, 0.0F, 0.0F);
            cube(mask, "front", 6, 20, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 1.0F,
                    -1.0F, -3.2F + y, -6.0F, 0.0F, 0.0F, 0.0F);
            cube(filter, "connector", 6, 23, 0.0F, 0.0F, -3.0F, 2.0F, 2.0F, 1.0F,
                    -1.0F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
            cube(filter, "filter_tall", 18, 21, 0.0F, -1.0F, -5.0F, 3.0F, 4.0F, 2.0F,
                    -1.5F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
            cube(filter, "filter_wide", 18, 16, 0.0F, -0.5F, -5.0F, 4.0F, 3.0F, 2.0F,
                    -2.0F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
            return LayerDefinition.create(mesh, 32, 32);
        }

        private static void cube(PartDefinition parent, String name, int u, int v, float x, float y, float z,
                float dx, float dy, float dz, float pivotX, float pivotY, float pivotZ,
                float rotX, float rotY, float rotZ) {
            parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, dx, dy, dz),
                    PartPose.offsetAndRotation(pivotX, pivotY, pivotZ, rotX, rotY, rotZ));
        }
    }
}
