package com.hbm.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy 1.7.10 package bridge for packet buffer helpers.
 */
@Deprecated(forRemoval = false)
public final class BufferUtil {
    private BufferUtil() {
    }

    public static void writeString(FriendlyByteBuf buffer, String value) {
        com.hbm.ntm.util.BufferUtil.writeString(buffer, value);
    }

    public static String readString(FriendlyByteBuf buffer) {
        return com.hbm.ntm.util.BufferUtil.readString(buffer);
    }

    public static void writeIntArray(FriendlyByteBuf buffer, int[] array) {
        com.hbm.ntm.util.BufferUtil.writeIntArray(buffer, array);
    }

    public static int[] readIntArray(FriendlyByteBuf buffer) {
        return com.hbm.ntm.util.BufferUtil.readIntArray(buffer);
    }

    public static void writeVec3(FriendlyByteBuf buffer, Vec3 vector) {
        com.hbm.ntm.util.BufferUtil.writeVec3(buffer, vector);
    }

    public static Vec3 readVec3(FriendlyByteBuf buffer) {
        return com.hbm.ntm.util.BufferUtil.readVec3(buffer);
    }

    public static void writeNBT(FriendlyByteBuf buffer, CompoundTag tag) {
        com.hbm.ntm.util.BufferUtil.writeNBT(buffer, tag);
    }

    public static CompoundTag readNBT(FriendlyByteBuf buffer) {
        return com.hbm.ntm.util.BufferUtil.readNBT(buffer);
    }

    public static void writeItemStack(FriendlyByteBuf buffer, ItemStack stack) {
        com.hbm.ntm.util.BufferUtil.writeItemStack(buffer, stack);
    }

    public static ItemStack readItemStack(FriendlyByteBuf buffer) {
        return com.hbm.ntm.util.BufferUtil.readItemStack(buffer);
    }
}
