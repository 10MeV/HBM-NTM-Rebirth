package com.hbm.ntm.api.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public interface AnalyzableBlock {
    List<Component> getDebugInfo(Level level, BlockPos pos);
}
