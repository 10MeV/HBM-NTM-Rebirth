package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.core.BlockPos;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public record PneumaticItemAccess(IItemHandler handler, @Nullable BlockPos pos) {
}
