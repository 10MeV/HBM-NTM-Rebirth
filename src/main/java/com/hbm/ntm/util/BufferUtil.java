package com.hbm.ntm.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy-name facade for packet buffer helpers.
 */
@Deprecated(forRemoval = false)
public final class BufferUtil {
    private BufferUtil() {
    }

    public static void writeString(FriendlyByteBuf buffer, String value) {
        HbmBufferUtil.writeString(buffer, value);
    }

    public static String readString(FriendlyByteBuf buffer) {
        return HbmBufferUtil.readString(buffer);
    }

    public static void writeIntArray(FriendlyByteBuf buffer, int[] array) {
        HbmBufferUtil.writeIntArray(buffer, array);
    }

    public static int[] readIntArray(FriendlyByteBuf buffer) {
        return HbmBufferUtil.readIntArray(buffer);
    }

    public static void writeVec3(FriendlyByteBuf buffer, Vec3 vector) {
        HbmBufferUtil.writeVec3(buffer, vector);
    }

    public static Vec3 readVec3(FriendlyByteBuf buffer) {
        return HbmBufferUtil.readVec3(buffer);
    }

    public static void writeNBT(FriendlyByteBuf buffer, CompoundTag tag) {
        HbmBufferUtil.writeNbt(buffer, tag);
    }

    public static CompoundTag readNBT(FriendlyByteBuf buffer) {
        return HbmBufferUtil.readNbt(buffer);
    }

    public static void writeItemStack(FriendlyByteBuf buffer, ItemStack stack) {
        HbmBufferUtil.writeItemStack(buffer, stack);
    }

    public static ItemStack readItemStack(FriendlyByteBuf buffer) {
        return HbmBufferUtil.readItemStack(buffer);
    }
}
