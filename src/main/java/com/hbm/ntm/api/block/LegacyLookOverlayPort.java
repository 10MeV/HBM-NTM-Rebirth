package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public record LegacyLookOverlayPort(BlockPos offset, Direction direction, Supplier<List<Component>> lines) {
    public boolean matches(BlockPos corePos, BlockPos viewedPos) {
        return viewedPos.relative(direction).equals(corePos.offset(offset));
    }
}
