package com.hbm.ntm.api.block;

import com.hbm.ntm.fluid.HbmFluidTank;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public interface HbmPersistentBlockState {
    String TAG_PERSISTENT = "persistent";

    void writePersistentState(CompoundTag persistent);

    void readPersistentState(CompoundTag persistent);

    default void writePersistentStateToStack(ItemStack stack) {
        CompoundTag persistent = new CompoundTag();
        writePersistentState(persistent);
        if (!persistent.isEmpty()) {
            stack.getOrCreateTag().put(TAG_PERSISTENT, persistent);
        }
    }

    default void readPersistentStateFromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            readPersistentState(tag.getCompound(TAG_PERSISTENT));
        }
    }

    static void writeIndexedTanks(CompoundTag persistent, List<HbmFluidTank> tanks) {
        if (tanks.stream().allMatch(tank -> tank.getFill() == 0)) {
            return;
        }
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).writeToNbt(persistent, Integer.toString(i));
        }
    }

    static void readIndexedTanks(CompoundTag persistent, List<HbmFluidTank> tanks) {
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).readFromNbt(persistent, Integer.toString(i));
        }
    }
}
