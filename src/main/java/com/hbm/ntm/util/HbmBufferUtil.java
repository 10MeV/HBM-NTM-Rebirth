package com.hbm.ntm.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;

public final class HbmBufferUtil {
    private HbmBufferUtil() {
    }

    public static void writeString(FriendlyByteBuf buffer, String value) {
        if (value == null) {
            buffer.writeInt(-1);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readString(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        if (count < 0) {
            return null;
        }
        byte[] bytes = new byte[count];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeIntArray(FriendlyByteBuf buffer, int[] array) {
        int[] values = array == null ? new int[0] : array;
        buffer.writeInt(values.length);
        for (int value : values) {
            buffer.writeInt(value);
        }
    }

    public static int[] readIntArray(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        int[] array = new int[Math.max(0, length)];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.readInt();
        }
        return array;
    }

    public static void writeVec3(FriendlyByteBuf buffer, Vec3 vector) {
        buffer.writeBoolean(vector != null);
        if (vector == null) {
            return;
        }
        buffer.writeDouble(vector.x);
        buffer.writeDouble(vector.y);
        buffer.writeDouble(vector.z);
    }

    public static Vec3 readVec3(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeNbt(FriendlyByteBuf buffer, CompoundTag tag) {
        buffer.writeNbt(tag);
    }

    public static CompoundTag readNbt(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return tag == null ? new CompoundTag() : tag;
    }

    public static void writeItemStack(FriendlyByteBuf buffer, ItemStack stack) {
        buffer.writeItem(stack == null ? ItemStack.EMPTY : stack);
    }

    public static ItemStack readItemStack(FriendlyByteBuf buffer) {
        return buffer.readItem();
    }
}
