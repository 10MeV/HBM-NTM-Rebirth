package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.LegacyRedstoneSwordItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Exact cuboid port of {@code ModelSword} for legacy redstone-sword direct contexts. */
public final class LegacyRedstoneSwordItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/model_sword_redstone.png");
    private static final ModelPart MODEL = createModel();

    public static final LegacyRedstoneSwordItemRenderer INSTANCE = new LegacyRedstoneSwordItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private LegacyRedstoneSwordItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LegacyRedstoneSwordItem)) {
            return;
        }
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, -135.0F);
        poseStack.translate(-0.8D, 0.4D, -0.1D);
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        MODEL.render(poseStack, vertices, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("grip_bottom", CubeListBuilder.create().texOffs(0, 17)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("grip_handle", CubeListBuilder.create().texOffs(8, 2)
                .addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 1.0F), PartPose.offset(0.5F, -5.0F, 0.0F));
        root.addOrReplaceChild("shield", CubeListBuilder.create().texOffs(14, 5)
                .addBox(0.0F, 0.0F, 0.0F, 6.0F, 1.0F, 3.0F), PartPose.offset(-1.5F, -6.0F, -1.0F));
        root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 16.0F, 1.0F), PartPose.offset(0.0F, -22.0F, 0.0F));
        root.addOrReplaceChild("blade_tip", CubeListBuilder.create().texOffs(8, 0)
                .addBox(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F), PartPose.offset(0.5F, -23.0F, 0.0F));
        root.addOrReplaceChild("shield_one", CubeListBuilder.create().texOffs(14, 0)
                .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 4.0F), PartPose.offset(-2.0F, -6.5F, -1.5F));
        root.addOrReplaceChild("shield_two", CubeListBuilder.create().texOffs(24, 0)
                .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 4.0F), PartPose.offset(4.0F, -6.5F, -1.5F));
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }
}
