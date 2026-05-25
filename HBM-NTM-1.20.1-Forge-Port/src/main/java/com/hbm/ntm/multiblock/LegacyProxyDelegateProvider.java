package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 TileEntityProxyDyn-style hook for proxy positions that should target a delegate instead of the core.
 */
public interface LegacyProxyDelegateProvider {
    @Nullable
    ICapabilityProvider getLegacyProxyDelegate(BlockPos proxyPos);
}
