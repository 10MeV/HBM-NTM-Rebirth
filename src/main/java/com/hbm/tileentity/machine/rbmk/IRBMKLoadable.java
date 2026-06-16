package com.hbm.tileentity.machine.rbmk;

import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for RBMK crane/autoloader item exchange.
 */
@Deprecated(forRemoval = false)
public interface IRBMKLoadable {
    boolean canLoad(ItemStack toLoad);

    void load(ItemStack toLoad);

    boolean canUnload();

    ItemStack provideNext();

    void unload();
}
