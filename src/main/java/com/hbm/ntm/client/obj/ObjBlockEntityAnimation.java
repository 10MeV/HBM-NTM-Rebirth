package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface ObjBlockEntityAnimation<T extends BlockEntity> {
    void apply(T blockEntity, float partialTick, PoseStack poseStack);
}
