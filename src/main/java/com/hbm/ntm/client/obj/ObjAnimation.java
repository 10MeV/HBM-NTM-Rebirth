package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface ObjAnimation {
    void apply(PoseStack poseStack, float partialTick);
}
