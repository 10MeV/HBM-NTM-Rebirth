package com.hbm.ntm.client.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundLoopMachine extends HbmDynamicSound {
    private final BlockEntity blockEntity;

    public SoundLoopMachine(ResourceLocation location, BlockEntity blockEntity) {
        this(location, blockEntity, SoundSource.BLOCKS);
    }

    public SoundLoopMachine(ResourceLocation location, BlockEntity blockEntity, SoundSource source) {
        this(location, blockEntity, source, 0.5D, 0.5D, 0.5D);
    }

    public SoundLoopMachine(ResourceLocation location, BlockEntity blockEntity, SoundSource source,
            double offsetX, double offsetY, double offsetZ) {
        super(location, source);
        this.blockEntity = blockEntity;
        setPosition(blockEntity.getBlockPos().getX() + offsetX, blockEntity.getBlockPos().getY() + offsetY,
                blockEntity.getBlockPos().getZ() + offsetZ);
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
