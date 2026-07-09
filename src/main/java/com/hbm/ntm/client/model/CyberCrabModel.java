package com.hbm.ntm.client.model;

import com.hbm.ntm.entity.mob.EntityCyberCrab;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class CyberCrabModel<T extends EntityCyberCrab> extends EntityModel<T> {
    private static final String[] PART_NAMES = {
            "box1", "box2", "box3", "box4", "box5", "leg6", "leg7", "leg8", "leg9", "foot10",
            "foot11", "foot12", "foot13", "fang14", "fang15", "fang16", "fang17", "box18", "box19", "box20"
    };

    private final ModelPart root;
    private final ModelPart[] parts = new ModelPart[PART_NAMES.length];

    public CyberCrabModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < PART_NAMES.length; i++) {
            parts[i] = root.getChild(PART_NAMES[i]);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        add(root, 0, 1, 1, 0.0F, 0.0F, 0.0F, 4, 1, 4, -2.0F, -3.0F, -2.0F, 0.0F, 0.0F, 0.0F);
        add(root, 1, 17, 1, 0.0F, 0.0F, 0.0F, 4, 1, 6, -2.0F, -4.0F, -3.0F, 0.0F, 0.0F, 0.0F);
        add(root, 2, 33, 1, 0.0F, 0.0F, 0.0F, 3, 1, 3, -1.5F, -5.0F, -1.5F, 0.0F, 0.0F, 0.0F);
        add(root, 3, 49, 1, 0.0F, 0.0F, 0.0F, 4, 1, 2, -2.0F, -4.5F, -1.0F, 0.0F, 0.0F, 0.0F);
        add(root, 4, 1, 9, 0.0F, 0.0F, 0.0F, 6, 1, 4, -3.0F, -4.0F, -2.0F, 0.0F, 0.0F, 0.0F);
        add(root, 5, 25, 9, -0.5F, 0.0F, 2.0F, 1, 1, 3, 0.0F, -3.0F, 0.0F, -0.17453293F, 0.78539816F, 0.0F);
        add(root, 6, 41, 9, -0.5F, 0.0F, 2.0F, 1, 1, 3, 0.0F, -3.0F, 0.0F, -0.17453293F, -0.78539816F, 0.0F);
        add(root, 7, 1, 17, -0.5F, 0.0F, 2.0F, 1, 1, 3, 0.0F, -3.0F, 0.0F, -0.17453293F, -2.35619449F, 0.0F);
        add(root, 8, 17, 17, -0.5F, 0.0F, 2.0F, 1, 1, 3, 0.0F, -3.0F, 0.0F, -0.17453293F, 2.35619449F, 0.0F);
        add(root, 9, 57, 9, -0.5F, 1.0F, 4.0F, 1, 3, 1, 0.0F, -3.0F, 0.0F, 0.17453293F, -0.78539816F, 0.0F);
        add(root, 10, 33, 17, -0.5F, 1.0F, 4.0F, 1, 3, 1, 0.0F, -3.0F, 0.0F, 0.17453293F, 0.78539816F, 0.0F);
        add(root, 11, 41, 17, -0.5F, 1.0F, 4.0F, 1, 3, 1, 0.0F, -3.0F, 0.0F, 0.17453293F, -2.35619449F, 0.0F);
        add(root, 12, 49, 17, -0.5F, 1.0F, 4.0F, 1, 3, 1, 0.0F, -3.0F, 0.0F, 0.17453293F, 2.35619449F, 0.0F);
        add(root, 13, 17, 1, -0.5F, 0.0F, 1.5F, 1, 1, 1, 0.0F, -3.0F, 0.0F, -0.43633231F, -0.6981317F, 0.0F);
        add(root, 14, 33, 9, -0.5F, 0.0F, 1.5F, 1, 1, 1, 0.0F, -3.0F, 0.0F, -0.43633231F, 0.87266463F, 0.0F);
        add(root, 15, 49, 9, -0.5F, 0.0F, 1.5F, 1, 1, 1, 0.0F, -3.0F, 0.0F, -0.43633231F, -2.26892803F, 0.0F);
        add(root, 16, 9, 17, -0.5F, 0.0F, 1.5F, 1, 1, 1, 0.0F, -3.0F, 0.0F, -0.43633231F, 2.44346095F, 0.0F);
        add(root, 17, 1, 25, 0.0F, 0.0F, 0.0F, 2, 1, 4, -1.0F, -4.5F, -2.0F, 0.0F, 0.0F, 0.0F);
        add(root, 18, 17, 25, 0.0F, 0.0F, 0.0F, 5, 1, 3, -2.5F, -3.5F, -1.5F, 0.0F, 0.0F, 0.0F);
        add(root, 19, 33, 25, 0.0F, 0.0F, 0.0F, 3, 1, 5, -1.5F, -3.5F, -2.5F, 0.0F, 0.0F, 0.0F);
        root.addOrReplaceChild("legacy_root", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -Mth.HALF_PI, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void add(PartDefinition root, int index, int texU, int texV,
            float boxX, float boxY, float boxZ, int sizeX, int sizeY, int sizeZ,
            float pivotX, float pivotY, float pivotZ, float xRot, float yRot, float zRot) {
        root.addOrReplaceChild(PART_NAMES[index],
                CubeListBuilder.create().texOffs(texU, texV).mirror()
                        .addBox(boxX, boxY, boxZ, sizeX, sizeY, sizeZ),
                PartPose.offsetAndRotation(pivotX, pivotY + 24.0F, pivotZ, xRot, yRot - Mth.HALF_PI, zRot));
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        parts[10].yRot = 0.78539816F - Mth.HALF_PI;
        parts[9].yRot = -0.78539816F - Mth.HALF_PI;
        parts[11].yRot = -2.35619449F - Mth.HALF_PI;
        parts[12].yRot = 2.35619449F - Mth.HALF_PI;
        float motion = (-(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount) * 1.5F;
        parts[10].yRot += motion;
        parts[9].yRot -= motion;
        parts[11].yRot -= motion;
        parts[12].yRot += motion;
        parts[5].yRot = parts[10].yRot;
        parts[6].yRot = parts[9].yRot;
        parts[7].yRot = parts[11].yRot;
        parts[8].yRot = parts[12].yRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
