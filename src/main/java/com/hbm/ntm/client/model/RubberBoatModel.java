package com.hbm.ntm.client.model;

import com.hbm.ntm.entity.item.RubberBoatEntity;
import java.util.List;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class RubberBoatModel extends ListModel<RubberBoatEntity> {
    private final List<ModelPart> parts;

    public RubberBoatModel(ModelPart root) {
        this.parts = List.of(
                root.getChild("bottom"),
                root.getChild("left_end"),
                root.getChild("right_end"),
                root.getChild("right_side"),
                root.getChild("left_side"));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create()
                .texOffs(0, 8)
                .addBox(-12.0F, -8.0F, -3.0F, 24.0F, 16.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, (float) Math.PI / 2.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("left_end", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-10.0F, -7.0F, -1.0F, 20.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(-11.0F, 4.0F, 0.0F, 0.0F, (float) Math.PI * 1.5F, 0.0F));
        root.addOrReplaceChild("right_end", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-10.0F, -7.0F, -1.0F, 20.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(11.0F, 4.0F, 0.0F, 0.0F, (float) Math.PI / 2.0F, 0.0F));
        root.addOrReplaceChild("right_side", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-10.0F, -7.0F, -1.0F, 20.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, (float) Math.PI, 0.0F));
        root.addOrReplaceChild("left_side", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-10.0F, -7.0F, -1.0F, 20.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, 4.0F, 9.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(RubberBoatEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
    }

    @Override
    public Iterable<ModelPart> parts() {
        return parts;
    }
}
