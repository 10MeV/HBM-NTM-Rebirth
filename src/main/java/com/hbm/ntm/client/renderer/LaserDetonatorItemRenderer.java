package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.LaserDetonatorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.Random;

/** Exact legacy context transforms with the OBJ Main/Lights partition retained. */
public final class LaserDetonatorItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final LaserDetonatorItemRenderer INSTANCE = new LaserDetonatorItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private LaserDetonatorItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        if (!(stack.getItem() instanceof LaserDetonatorItem)) return;
        poseStack.pushPose();
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.scale(0.25F, 0.25F, 0.25F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 80.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, -20.0F);
                poseStack.translate(1.0D, 0.5D, 3.0D);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.scale(-0.125F, -0.125F, -0.125F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 85.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, 145.0F);
                poseStack.translate(-0.5D, -1.0D, 6.5D);
            }
            case GROUND -> {
                LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
                poseStack.scale(0.25F, 0.25F, 0.25F);
            }
            case GUI -> {
                poseStack.scale(3.5F, 3.5F, -3.5F);
                poseStack.translate(1.5D, 2.75D, 0.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, -45.0F);
            }
            default -> { }
        }
        ObjWeaponModels.DETONATOR_LASER.renderPart("Main", ObjWeaponModels.DETONATOR_LASER_TEXTURE,
                poseStack, buffer, light, overlay);
        ObjWeaponModels.DETONATOR_LASER.renderPart("Lights", ObjWeaponModels.DETONATOR_LASER_TEXTURE,
                poseStack, buffer, LightTexture.FULL_BRIGHT, overlay);
        renderLegacyScreen(poseStack, buffer, overlay);
        poseStack.popPose();
    }

    /**
     * Direct migration of ItemRenderDetonatorLaser's untextured sinusoidal
     * yellow screen trace and its three 500 ms randomised red number rows.
     */
    private static void renderLegacyScreen(PoseStack poseStack, MultiBufferSource buffer, int overlay) {
        final float pixel = 0.0625F;
        poseStack.pushPose();
        poseStack.translate(0.5626D, pixel * 18.0D, -pixel * 14.0D);
        VertexConsumer vertices = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        final int subdivisions = 32;
        final float width = pixel * 8.0F;
        final float segment = width / subdivisions;
        final double time = System.currentTimeMillis() / -100.0D;
        final double amplitude = 0.075D;
        for (int index = 0; index < subdivisions; index++) {
            float h0 = (float) (Math.sin(index * 0.5D + time) * amplitude);
            float h1 = (float) (Math.sin((index + 1) * 0.5D + time) * amplitude);
            float z0 = segment * index;
            float z1 = segment * (index + 1);
            vertex(vertices, matrix, 0.0F, -pixel * 0.25F + h1, z1);
            vertex(vertices, matrix, 0.0F, pixel * 0.25F + h1, z1);
            vertex(vertices, matrix, 0.0F, pixel * 0.25F + h0, z0);
            vertex(vertices, matrix, 0.0F, -pixel * 0.25F + h0, z0);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5625D, 1.3125D, 0.875D);
        poseStack.scale(0.01F, -0.01F, 0.01F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.translate(3.0D, -2.0D, 0.2D);
        Font font = Minecraft.getInstance().font;
        Random random = new Random(System.currentTimeMillis() / 500L);
        for (int index = 0; index < 3; index++) {
            String row = Integer.toString(random.nextInt(900_000) + 100_000);
            font.drawInBatch(row, 0.0F, 0.0F, 0xFFFF0000, false, poseStack.last().pose(), buffer,
                    Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.translate(0.0D, 12.5D, 0.0D);
        }
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, float x, float y, float z) {
        vertices.vertex(matrix, x, y, z).color(255, 255, 0, 255).endVertex();
    }
}
