package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface PneumaticEndpoint {
    boolean matchesFilter(ItemStack stack);

    boolean isWhitelist();

    BlockPos getPneumaticPos();

    default int itemHardCap() {
        return PneumaticNetwork.ITEMS_PER_TRANSFER;
    }

    default boolean isPneumaticLoaded() {
        return !(this instanceof BlockEntity blockEntity) || !blockEntity.isRemoved();
    }
}
