package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.core.BlockPos;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Capability adaptation of a legacy pneumatic {@code IInventory} target.
 *
 * <p>The legacy sender applied its per-transfer hard cap to the destination
 * inventory itself (not to the endpoint tube).  Carry that source-backed
 * destination property alongside the modern handler so the network does not
 * need to infer a block entity from an arbitrary capability wrapper.</p>
 */
public record PneumaticItemAccess(IItemHandler handler, @Nullable BlockPos pos, int itemHardCap) {
    public PneumaticItemAccess(IItemHandler handler, @Nullable BlockPos pos) {
        this(handler, pos, PneumaticNetwork.ITEMS_PER_TRANSFER);
    }

    public PneumaticItemAccess {
        itemHardCap = Math.max(1, itemHardCap);
    }
}
