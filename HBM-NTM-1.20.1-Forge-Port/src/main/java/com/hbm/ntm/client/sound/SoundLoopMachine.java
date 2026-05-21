package com.hbm.ntm.client.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundLoopMachine extends HbmDynamicSound {
    private final BlockEntity blockEntity;

    public SoundLoopMachine(ResourceLocation location, BlockEntity blockEntity) {
        super(location);
        this.blockEntity = blockEntity;
        setPosition(blockEntity.getBlockPos().getX() + 0.5D, blockEntity.getBlockPos().getY() + 0.5D, blockEntity.getBlockPos().getZ() + 0.5D);
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved()) {
            requestStop();
            return;
        }
        super.tick();
    }
}
