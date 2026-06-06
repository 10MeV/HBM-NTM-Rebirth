package com.hbm.ntm.blockentity;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface PaintableDuctBlockEntity {
    @Nullable
    BlockState getPaintedState();

    int getPaintedMeta();

    void setPaintedState(@Nullable BlockState state, int legacyMeta);

    default boolean hasPaintedState() {
        return getPaintedState() != null;
    }
}
