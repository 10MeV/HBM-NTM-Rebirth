package com.hbm.ntm.client.model;

import com.hbm.ntm.entity.mob.EntityPigeon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Exact cuboid and fat-body layout from 1.7.10's {@code ModelPigeon}. */
public final class PigeonModel extends EntityModel<EntityPigeon> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart body;
    private final ModelPart bodyFat;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart ass;
    private final ModelPart feathers;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart fatLeftWing;
    private final ModelPart fatRightWing;

    public PigeonModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        beak = root.getChild("beak");
        body = root.getChild("body");
        bodyFat = root.getChild("body_fat");
        leftLeg = root.getChild("left_leg");
        rightLeg = root.getChild("right_leg");
        ass = root.getChild("ass");
        feathers = root.getChild("feathers");
        leftWing = body.getChild("left_wing");
        rightWing = body.getChild("right_wing");
        fatLeftWing = bodyFat.getChild("left_wing");
        fatRightWing = bodyFat.getChild("right_wing");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(0.0F, 16.0F, -2.0F));
        root.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(14, 0)
                .addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 16.0F, -2.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-3.0F, -3.0F, -4.0F, 6.0F, 6.0F, 8.0F), PartPose.offset(0.0F, 17.0F, 0.0F));
        PartDefinition bodyFat = root.addOrReplaceChild("body_fat", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-3.0F, -3.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(1.0F)),
                PartPose.offset(0.0F, 17.0F, 0.0F));
        addWings(body, 3.0F, -3.0F);
        addWings(bodyFat, 4.0F, -4.0F);
        root.addOrReplaceChild("ass", CubeListBuilder.create().texOffs(0, 24)
                .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 20.0F, 4.0F));
        root.addOrReplaceChild("feathers", CubeListBuilder.create().texOffs(16, 24)
                .addBox(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 21.5F, 7.5F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(20, 0)
                .addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(1.0F, 20.0F, -1.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 0)
                .addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(-1.0F, 20.0F, -1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void addWings(PartDefinition body, float leftX, float rightX) {
        body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(28, 0)
                .addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(leftX, -2.0F, 0.0F));
        body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(28, 10)
                .addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(rightX, -2.0F, 0.0F));
    }

    @Override
    public void setupAnim(EntityPigeon pigeon, float limbSwing, float limbSwingAmount, float flap,
            float netHeadYaw, float headPitch) {
        float headX = headPitch * Mth.DEG_TO_RAD;
        float headY = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = beak.xRot = headX;
        head.yRot = beak.yRot = headY;
        body.xRot = bodyFat.xRot = ass.xRot = -Mth.PI / 4.0F;
        feathers.xRot = -Mth.PI / 8.0F;
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        leftWing.zRot = fatLeftWing.zRot = flap;
        rightWing.zRot = fatRightWing.zRot = -flap;

        boolean fat = pigeon.isFat();
        body.visible = !fat;
        bodyFat.visible = fat;
        head.z = beak.z = fat ? -4.0F : -2.0F;
        ass.z = fat ? 5.0F : 4.0F;
        feathers.z = fat ? 8.5F : 7.5F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
