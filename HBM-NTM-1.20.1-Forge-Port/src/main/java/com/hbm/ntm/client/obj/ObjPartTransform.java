package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;

public record ObjPartTransform(double pivotX, double pivotY, double pivotZ, float scaleX, float scaleY, float scaleZ) {
    public static final ObjPartTransform IDENTITY = new ObjPartTransform(0.0D, 0.0D, 0.0D, 1.0F, 1.0F, 1.0F);
    public static final ObjPartTransform BLOCK_CENTER = new ObjPartTransform(0.5D, 0.0D, 0.5D, 1.0F, 1.0F, 1.0F);

    public ObjPartTransform withScale(float x, float y, float z) {
        return new ObjPartTransform(pivotX, pivotY, pivotZ, x, y, z);
    }

    public void apply(PoseStack poseStack) {
        poseStack.translate(pivotX, pivotY, pivotZ);
        poseStack.scale(scaleX, scaleY, scaleZ);
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
    }
}
