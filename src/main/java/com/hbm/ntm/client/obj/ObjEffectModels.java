package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class ObjEffectModels {
    public static final LegacyWavefrontModel SPHERE_RUV = model("sphere_ruv");
    public static final LegacyWavefrontModel SPHERE_IUV = model("sphere_iuv");
    public static final LegacyWavefrontModel SPHERE_UV = model("sphere_uv");
    public static final LegacyWavefrontModel SPHERE_NEW = model("sphere_new").asVBO();
    public static final LegacyWavefrontModel SPHERE = model("sphere");
    public static final LegacyWavefrontModel RING = model("ring", effectTexture("emp_blast"));
    public static final ResourceLocation EMP_BLAST_TEXTURE = effectTexture("emp_blast");
    public static final ResourceLocation BLACK_HOLE_TEXTURE = effectTexture("black_hole");
    public static final ResourceLocation CASINGS_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/casings.png");
    public static final LegacyWavefrontModel CASINGS = new LegacyWavefrontModel(
            new ResourceLocation(HbmNtm.MOD_ID, "models/effect/casings.obj"),
            CASINGS_TEXTURE).asVBO();

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(new ResourceLocation(HbmNtm.MOD_ID, "models/" + name + ".obj")).asVBO();
    }

    public static LegacyWavefrontModel model(String name, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/" + name + ".obj"),
                texture).asVBO();
    }

    public static ResourceLocation effectTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
    }

    public static void renderSphereNewDynamicUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive) {
        renderDynamicUntextured(SPHERE_NEW, poseStack, buffer, red, green, blue, alpha, additive);
    }

    public static void renderSphereNewDynamicUntextured(VertexConsumer consumer, Matrix4f position,
            int red, int green, int blue, int alpha) {
        renderDynamicUntextured(SPHERE_NEW, consumer, position, red, green, blue, alpha);
    }

    public static void renderSphereUvDynamicUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive) {
        renderDynamicUntextured(SPHERE_UV, poseStack, buffer, red, green, blue, alpha, additive);
    }

    public static void renderSphereUvDynamicUntextured(VertexConsumer consumer, Matrix4f position,
            int red, int green, int blue, int alpha) {
        renderDynamicUntextured(SPHERE_UV, consumer, position, red, green, blue, alpha);
    }

    public static VertexConsumer dynamicUntexturedConsumer(MultiBufferSource buffer, int alpha, boolean additive) {
        LegacyTexturedRenderMode renderMode = additive
                ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE
                : LegacyTexturedRenderMode.CUTOUT_NO_CULL;
        return buffer.getBuffer(LegacyUntexturedQuadRenderer.type(renderMode, alpha, VertexFormat.Mode.TRIANGLES));
    }

    private static void renderDynamicUntextured(LegacyWavefrontModel model, PoseStack poseStack,
            MultiBufferSource buffer, int red, int green, int blue, int alpha, boolean additive) {
        renderDynamicUntextured(model, dynamicUntexturedConsumer(buffer, alpha, additive), poseStack.last().pose(),
                red, green, blue, alpha);
    }

    private static void renderDynamicUntextured(LegacyWavefrontModel model, VertexConsumer consumer, Matrix4f position,
            int red, int green, int blue, int alpha) {
        model.renderAllUntextured(consumer, position, red, green, blue, alpha);
    }

    private ObjEffectModels() {
    }
}
