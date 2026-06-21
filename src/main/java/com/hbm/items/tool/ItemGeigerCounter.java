package com.hbm.items.tool;

import com.hbm.ntm.item.GeigerCounterItem;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Old-package source migration facade for the legacy Geiger counter item.
 */
@Deprecated(forRemoval = false)
public class ItemGeigerCounter extends GeigerCounterItem {
    public ItemGeigerCounter() {
        this(new Item.Properties().stacksTo(1));
    }

    public ItemGeigerCounter(Item.Properties properties) {
        super(properties);
    }

    static void setFloat(ItemStack stack, float value, String name) {
        stack.getOrCreateTag().putFloat(name, value);
    }

    public static float getFloat(ItemStack stack, String name) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0F : tag.getFloat(name);
    }

    public static int check(Level level, int x, int y, int z) {
        return (int) Math.ceil(ChunkRadiationManager.getRadiation(level, new BlockPos(x, y, z)));
    }
}
