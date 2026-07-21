package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.LegacyBigSwordItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Direct modern port of the six cuboids in {@code ModelBigSword} and the old
 * held/entity transform from {@code ItemRenderBigSword}.
 */
public final class LegacyBigSwordItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/ModelBigSwordTexture.png");
    private static final ModelPart MODEL = createModel();

    public static final LegacyBigSwordItemRenderer INSTANCE = new LegacyBigSwordItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private LegacyBigSwordItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LegacyBigSwordItem)) {
            return;
        }

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack);
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        MODEL.render(poseStack, vertices, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        LegacyPoseRotations.rotateZDegrees(poseStack, -135.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.translate(0.0D, 0.4D, -0.7D);
    }

    private static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("handle_bottom", CubeListBuilder.create().texOffs(1, 1).addBox(0.0F, 0.0F, 0.0F, 1.0F,
                3.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7853982F, 0.0F, 0.0F));
        root.addOrReplaceChild("handle_grip", CubeListBuilder.create().texOffs(17, 1).addBox(0.0F, 0.0F, 0.0F, 1.0F,
                5.0F, 2.0F), PartPose.offset(0.0F, -4.0F, -1.0F));
        root.addOrReplaceChild("handle_one", CubeListBuilder.create().texOffs(25, 1).addBox(0.0F, -1.0F, 0.0F, 2.0F,
                1.0F, 4.0F), PartPose.offsetAndRotation(-0.5F, -3.0F, 0.0F, 0.2617994F, 0.0F, 0.0F));
        root.addOrReplaceChild("handle_two", CubeListBuilder.create().texOffs(41, 1).addBox(0.0F, -1.0F, -4.0F, 2.0F,
                1.0F, 4.0F), PartPose.offsetAndRotation(-0.5F, -3.0F, 0.0F, -0.2617994F, 0.0F, 0.0F));
        root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(57, 1).addBox(0.0F, 0.0F, 0.0F, 3.0F,
                18.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -22.0F, 1.5F, 0.0F, 1.570796F, 0.0F));
        root.addOrReplaceChild("blade_tip", CubeListBuilder.create().texOffs(2, 10).addBox(0.0F, 0.0F, 0.0F, 1.0F,
                2.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -23.5F, 0.0F, -0.7853982F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }
}
